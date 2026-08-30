package io.github.tatselkrik.roomdeck

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import io.github.tatselkrik.roomdeck.data.ReceiverClient
import io.github.tatselkrik.roomdeck.data.ReceiverException
import io.github.tatselkrik.roomdeck.data.TvAppItem
import io.github.tatselkrik.roomdeck.data.TvProfile

import io.github.tatselkrik.roomdeck.data.TvStore
import io.github.tatselkrik.roomdeck.remote.AndroidTvDevice
import io.github.tatselkrik.roomdeck.remote.AndroidTvKey
import io.github.tatselkrik.roomdeck.remote.AndroidTvRemote
import io.github.tatselkrik.roomdeck.remote.AndroidTvState
import io.github.tatselkrik.roomdeck.remote.TailscaleRoute
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout
import java.util.concurrent.ConcurrentHashMap

enum class RoomDeckTab { REMOTE, APPS }
enum class PairingKind { ANDROID_TV_REMOTE }
enum class ReceiverLinkState { IDLE, CONNECTING, CONNECTED, UNAVAILABLE }

data class PairingPrompt(
    val kind: PairingKind,
    val profileId: String?,
    val title: String,
    val instruction: String,
    val acceptsCode: Boolean,
)

class RoomDeckViewModel(application: Application) : AndroidViewModel(application) {
    private val store = TvStore(application)
    private val receiverClient = ReceiverClient(application)
    private val remotes = ConcurrentHashMap<String, AndroidTvRemote>()
    private val remoteCollectors = ConcurrentHashMap<String, Job>()
    private val receiverSyncJobs = ConcurrentHashMap<String, Job>()
    private val appIconJobs = ConcurrentHashMap<String, Job>()
    private var pendingRemoteCode: CompletableDeferred<String?>? = null
    private var pairingJob: Job? = null

    private val mutableProfiles = MutableStateFlow(store.profiles())
    val profiles = mutableProfiles.asStateFlow()


    private val mutableSelectedProfileId = MutableStateFlow<String?>(null)
    val selectedProfileId = mutableSelectedProfileId.asStateFlow()

    private val mutableTab = MutableStateFlow(RoomDeckTab.REMOTE)
    val tab = mutableTab.asStateFlow()

    private val mutableRemoteStates = MutableStateFlow<Map<String, AndroidTvState>>(emptyMap())
    val remoteStates = mutableRemoteStates.asStateFlow()

    private val mutableReceiverStates = MutableStateFlow<Map<String, ReceiverLinkState>>(emptyMap())
    val receiverStates = mutableReceiverStates.asStateFlow()

    private val mutablePairingPrompt = MutableStateFlow<PairingPrompt?>(null)
    val pairingPrompt = mutablePairingPrompt.asStateFlow()


    private val mutableApps = MutableStateFlow<List<TvAppItem>>(emptyList())
    val apps = mutableApps.asStateFlow()


    private val mutableBusy = MutableStateFlow<String?>(null)
    val busy = mutableBusy.asStateFlow()

    private val mutableNotice = MutableStateFlow<String?>(null)
    val notice = mutableNotice.asStateFlow()

    init {
        val tailscaleProfiles = mutableProfiles.value.filter { TailscaleRoute.isIpv4Address(it.host) }
        tailscaleProfiles.forEach(::connectProfile)
        if (tailscaleProfiles.size != mutableProfiles.value.size) {
            mutableNotice.value =
                "RoomDeck now uses Tailscale only. Remove and re-add older TVs using their 100.x Tailscale address."
        }
    }

    fun selectProfile(profileId: String?) {
        val previousProfileId = mutableSelectedProfileId.value
        if (previousProfileId != profileId) {
            previousProfileId?.let { appIconJobs.remove(it)?.cancel() }
        }
        mutableSelectedProfileId.value = profileId
        mutableTab.value = RoomDeckTab.REMOTE
        mutableApps.value = emptyList()
        profileId?.let(::ensureReceiverSync)
    }

    fun setTab(next: RoomDeckTab) {
        mutableTab.value = next
        if (next == RoomDeckTab.APPS) {
            selectedProfile()?.let { profile ->
                val hasFreshCatalog =
                    mutableReceiverStates.value[profile.id] == ReceiverLinkState.CONNECTED &&
                        mutableApps.value.isNotEmpty()
                if (!hasFreshCatalog) ensureReceiverSync(profile.id)
            }
        }
    }

    fun beginRemotePairing(device: AndroidTvDevice, roomName: String) {
        if (pairingJob?.isActive == true) return
        val profile = store.fromDevice(device, roomName)
        val remote = AndroidTvRemote(getApplication(), device)
        remotes[profile.id] = remote
        attachRemoteState(profile.id, remote)
        pairingJob = viewModelScope.launch {
            mutableBusy.value = "Starting secure TV pairing…"
            mutablePairingPrompt.value = PairingPrompt(
                kind = PairingKind.ANDROID_TV_REMOTE,
                profileId = profile.id,
                title = "Pair ${profile.roomName}",
                instruction = "Waiting for the TV to display its Android TV Remote code…",
                acceptsCode = false,
            )
            val result = remote.pair {
                val deferred = CompletableDeferred<String?>()
                pendingRemoteCode = deferred
                mutableBusy.value = null
                mutablePairingPrompt.value = PairingPrompt(
                    kind = PairingKind.ANDROID_TV_REMOTE,
                    profileId = profile.id,
                    title = "Pair ${profile.roomName}",
                    instruction = "Enter the six-character code shown by Android TV.",
                    acceptsCode = true,
                )
                deferred.await()
            }
            pendingRemoteCode = null
            mutablePairingPrompt.value = null
            mutableBusy.value = null
            result.fold(
                onSuccess = {
                    store.save(profile)
                    reloadProfiles()
                    mutableSelectedProfileId.value = profile.id
                    runCatching { synchronizeReceiver(profile, remote, showBusy = true) }
                        .onSuccess {
                            mutableNotice.value = "${profile.roomName} added to RoomDeck."
                        }
                        .onFailure {
                            mutableReceiverStates.value = mutableReceiverStates.value +
                                (profile.id to ReceiverLinkState.UNAVAILABLE)
                            mutableNotice.value =
                                "${profile.roomName} remote paired. RoomDeck will retry Receiver automatically; keep Receiver and Tailscale running on the TV."
                        }
                },
                onFailure = { error ->
                    val receiverReachable = runCatching {
                        receiverClient.device(profile.host)
                    }.isSuccess
                    val networkDetail = if (receiverReachable) {
                        " RoomDeck Receiver is reachable through Tailscale."
                    } else {
                        " RoomDeck Receiver was also unreachable through Tailscale."
                    }
                    mutableNotice.value = (error.message ?: "TV pairing failed") + networkDetail
                    remotes.remove(profile.id)?.disconnect()
                    remoteCollectors.remove(profile.id)?.cancel()
                },
            )
        }
    }

    fun beginManualPairing(host: String, roomName: String) {
        val cleanedHost = host.trim()
        if (cleanedHost.isBlank() || roomName.isBlank()) {
            mutableNotice.value = "Enter both the room name and the TV's Tailscale address."
            return
        }
        if (!TailscaleRoute.isIpv4Address(cleanedHost)) {
            mutableNotice.value =
                "Enter the 100.x Tailscale address shown by RoomDeck Receiver on the TV."
            return
        }
        beginRemotePairing(
            AndroidTvDevice(name = roomName.trim(), host = cleanedHost),
            roomName.trim(),
        )
    }

    fun beginReceiverPairing(profileId: String) {
        ensureReceiverSync(profileId, force = true, announceFailure = true)
    }
    fun submitPairingCode(code: String) {
        val prompt = mutablePairingPrompt.value ?: return
        val cleaned = code.trim()
        if (cleaned.length != 6) {
            mutableNotice.value = "Enter the complete six-character code."
            return
        }
        pendingRemoteCode?.complete(cleaned)
        mutablePairingPrompt.value = prompt.copy(
            instruction = "Finishing secure pairing…",
            acceptsCode = false,
        )
        mutableBusy.value = "Pairing Android TV Remote…"
    }
    fun cancelPairing() {
        pendingRemoteCode?.complete(null)
        pendingRemoteCode = null
        pairingJob?.cancel()
        pairingJob = null
        mutablePairingPrompt.value = null
        mutableBusy.value = null
    }

    fun sendKey(key: AndroidTvKey) {
        val profile = selectedProfile() ?: return
        viewModelScope.launch {
            runCatching {
                val remote = connectedRemote(profile)
                remote.sendKey(key)
            }.onFailure { mutableNotice.value = it.message ?: "TV command failed" }
        }
    }

    fun turnSelectedOff() {
        val profile = selectedProfile() ?: return
        viewModelScope.launch {
            mutableBusy.value = "Turning ${profile.roomName} off…"
            runCatching {
                connectedRemote(profile).sendKey(TV_POWER_OFF_KEY)
            }
                .onSuccess { mutableNotice.value = "Off command sent to ${profile.roomName}." }
                .onFailure { mutableNotice.value = it.message ?: "Unable to turn TV off" }
            mutableBusy.value = null
        }
    }

    fun allPowerOff() {
        viewModelScope.launch {
            mutableBusy.value = "Turning all TVs off…"
            val results = mutableProfiles.value.map { profile ->
                async {
                    runCatching {
                        connectedRemote(profile).sendKey(TV_POWER_OFF_KEY)
                    }
                }
            }.awaitAll()
            val failures = results.count { it.isFailure }
            mutableBusy.value = null
            mutableNotice.value = if (failures == 0) {
                "Off command sent to all TVs."
            } else {
                "Off command sent, but $failures TV(s) could not be reached."
            }
        }
    }

    fun refreshSelected() {
        val profile = selectedProfile() ?: return
        viewModelScope.launch {
            mutableBusy.value = "Refreshing ${profile.roomName}…"
            runCatching {
                remotes[profile.id]?.disconnect()
                connectedRemote(profile)
            }
                .onSuccess {
                    ensureReceiverSync(profile.id, force = true, announceFailure = false)
                    mutableNotice.value = "${profile.roomName} remote refreshed."
                }
                .onFailure { mutableNotice.value = it.message ?: "Unable to refresh TV connection" }
            mutableBusy.value = null
        }
    }

    fun launchApp(item: TvAppItem) {
        val profile = selectedProfile() ?: return
        mutableTab.value = RoomDeckTab.REMOTE
        viewModelScope.launch {
            mutableBusy.value = "Opening ${item.label}…"
            runCatching {
                val launchUri = receiverClient.launch(profile, item.packageName)
                connectedRemote(profile).launchApp(launchUri)
            }
                .onSuccess { mutableNotice.value = "Opening ${item.label} on ${profile.roomName}." }
                .onFailure { mutableNotice.value = it.message ?: "Unable to open app" }
            mutableBusy.value = null
        }
    }


    fun moveApp(draggedPackage: String, targetPackage: String) {
        val profile = selectedProfile() ?: return
        val reordered = moveAppBefore(mutableApps.value, draggedPackage, targetPackage)
        if (reordered == mutableApps.value) return
        mutableApps.value = reordered
        saveAppOrder(profile, reordered)
    }

    fun resetAppOrder() {
        val profile = selectedProfile() ?: return
        val reordered = defaultAppOrder(mutableApps.value)
        mutableApps.value = reordered
        saveAppOrder(profile, reordered)
        mutableNotice.value = "${profile.roomName} app order reset."
    }

    private fun saveAppOrder(profile: TvProfile, orderedApps: List<TvAppItem>) {
        store.save(profile.copy(appOrder = orderedApps.map(TvAppItem::packageName)))
        reloadProfiles()
    }
    fun removeSelectedProfile() {
        val profile = selectedProfile() ?: return
        remotes.remove(profile.id)?.unpair()
        remoteCollectors.remove(profile.id)?.cancel()
        receiverSyncJobs.remove(profile.id)?.cancel()
        appIconJobs.remove(profile.id)?.cancel()
        mutableReceiverStates.value = mutableReceiverStates.value - profile.id
        store.remove(profile.id)
        reloadProfiles()
        mutableSelectedProfileId.value = null
        mutableNotice.value = "${profile.roomName} removed from RoomDeck."
    }

    fun clearNotice() {
        mutableNotice.value = null
    }


    private suspend fun authorizeReceiver(
        profile: TvProfile,
        remote: AndroidTvRemote,
        showBusy: Boolean,
    ): TvProfile {
        if (showBusy) mutableBusy.value = "Connecting RoomDeck Receiver…"
        try {
            val secret = receiverClient.newEnrollmentSecret()
            val launchUri = receiverClient.startEnrollment(profile.host, secret)
            if (remote.state.value !is AndroidTvState.Active) remote.connect()
            check(remote.state.value is AndroidTvState.Active) { "TV remote is not connected" }
            remote.launchApp(launchUri)
            val token = withTimeout(RECEIVER_ENROLLMENT_TIMEOUT_MS) {
                while (true) {
                    try {
                        return@withTimeout receiverClient.completeEnrollment(profile.host, secret)
                    } catch (error: ReceiverException) {
                        if (error.statusCode != 409) throw error
                        delay(RECEIVER_ENROLLMENT_POLL_MS)
                    }
                }
                error("Receiver authorization timed out")
            }
            return profile.copy(receiverToken = token).also {
                store.save(it)
                reloadProfiles()
            }
        } finally {
            if (showBusy) mutableBusy.value = null
        }
    }

    private suspend fun synchronizeReceiver(
        originalProfile: TvProfile,
        remote: AndroidTvRemote,
        showBusy: Boolean,
    ): TvProfile {
        mutableReceiverStates.value = mutableReceiverStates.value +
            (originalProfile.id to ReceiverLinkState.CONNECTING)
        var profile = mutableProfiles.value.firstOrNull { it.id == originalProfile.id } ?: originalProfile
        var installedApps: List<TvAppItem>? = null
        if (profile.receiverToken != null) {
            try {
                installedApps = receiverClient.apps(profile)
            } catch (error: ReceiverException) {
                if (error.statusCode != 401) throw error
                profile = profile.copy(receiverToken = null).also {
                    store.save(it)
                    reloadProfiles()
                }
            }
        }
        if (profile.receiverToken == null) {
            profile = authorizeReceiver(profile, remote, showBusy)
        }
        val appsFromTv = installedApps ?: receiverClient.apps(profile)
        val orderedApps = applySavedAppOrder(appsFromTv, profile.appOrder)
        if (mutableSelectedProfileId.value == profile.id) {
            mutableApps.value = orderedApps
        }
        mutableReceiverStates.value = mutableReceiverStates.value +
            (profile.id to ReceiverLinkState.CONNECTED)
        if (mutableSelectedProfileId.value == profile.id) {
            startAppIconLoading(profile, orderedApps)
        }
        return profile
    }

    private fun startAppIconLoading(profile: TvProfile, apps: List<TvAppItem>) {
        appIconJobs.remove(profile.id)?.cancel()
        if (apps.isEmpty()) return
        val job = viewModelScope.launch {
            apps.chunked(APP_ICON_BATCH_SIZE).forEach { batch ->
                val loadedIcons = batch.map { app ->
                    async {
                        app.packageName to runCatching {
                            receiverClient.appIcon(profile, app.packageName)
                        }.getOrNull()
                    }
                }.awaitAll().toMap()
                if (mutableSelectedProfileId.value == profile.id) {
                    mutableApps.value = mergeAppIcons(mutableApps.value, loadedIcons)
                }
            }
        }
        appIconJobs[profile.id] = job
        job.invokeOnCompletion { appIconJobs.remove(profile.id, job) }
    }

    private fun ensureReceiverSync(
        profileId: String,
        force: Boolean = false,
        announceFailure: Boolean = false,
    ) {
        val existing = receiverSyncJobs[profileId]
        if (existing?.isActive == true && !force) return
        if (force) {
            existing?.cancel()
            appIconJobs.remove(profileId)?.cancel()
        }
        val profile = mutableProfiles.value.firstOrNull { it.id == profileId } ?: return
        mutableReceiverStates.value = mutableReceiverStates.value +
            (profileId to ReceiverLinkState.CONNECTING)
        val job = viewModelScope.launch {
            var lastError: Throwable? = null
            repeat(RECEIVER_SYNC_ATTEMPTS) { attempt ->
                val latest = mutableProfiles.value.firstOrNull { it.id == profileId } ?: return@launch
                val result = runCatching {
                    synchronizeReceiver(latest, connectedRemote(latest), showBusy = false)
                }
                if (result.isSuccess) return@launch
                lastError = result.exceptionOrNull()
                if (attempt < RECEIVER_SYNC_ATTEMPTS - 1) {
                    delay(RECEIVER_SYNC_RETRY_MS * (attempt + 1))
                }
            }
            mutableReceiverStates.value = mutableReceiverStates.value +
                (profileId to ReceiverLinkState.UNAVAILABLE)
            if (announceFailure) {
                mutableNotice.value = lastError?.message ?: "Unable to reach RoomDeck Receiver"
            }
        }
        receiverSyncJobs[profileId] = job
        job.invokeOnCompletion { receiverSyncJobs.remove(profileId, job) }
    }

    private suspend fun connectedRemote(profile: TvProfile): AndroidTvRemote {
        val remote = remotes[profile.id] ?: AndroidTvRemote(
            getApplication(),
            profile.asRemoteDevice(),
        ).also {
            remotes[profile.id] = it
            attachRemoteState(profile.id, it)
        }
        if (remote.state.value !is AndroidTvState.Active) remote.connect()
        check(remote.state.value is AndroidTvState.Active) { "TV is not reachable" }
        return remote
    }

    private fun connectProfile(profile: TvProfile) {
        val remote = AndroidTvRemote(getApplication(), profile.asRemoteDevice())
        remotes[profile.id] = remote
        attachRemoteState(profile.id, remote)
        viewModelScope.launch { remote.connect() }
    }

    private fun attachRemoteState(profileId: String, remote: AndroidTvRemote) {
        remoteCollectors.remove(profileId)?.cancel()
        remoteCollectors[profileId] = viewModelScope.launch {
            remote.state.collect { state ->
                mutableRemoteStates.value = mutableRemoteStates.value + (profileId to state)
                if (
                    state is AndroidTvState.Active &&
                    mutableSelectedProfileId.value == profileId
                ) {
                    ensureReceiverSync(profileId)
                }
            }
        }
    }

    private fun selectedProfile(): TvProfile? =
        mutableProfiles.value.firstOrNull { it.id == mutableSelectedProfileId.value }

    private fun reloadProfiles() {
        mutableProfiles.value = store.profiles()
    }


    override fun onCleared() {
        receiverSyncJobs.values.forEach(Job::cancel)
        appIconJobs.values.forEach(Job::cancel)
        remotes.values.forEach(AndroidTvRemote::disconnect)
        super.onCleared()
    }

    private companion object {
        const val RECEIVER_ENROLLMENT_TIMEOUT_MS = 15_000L
        const val RECEIVER_ENROLLMENT_POLL_MS = 350L
        const val RECEIVER_SYNC_ATTEMPTS = 3
        const val RECEIVER_SYNC_RETRY_MS = 750L
        const val APP_ICON_BATCH_SIZE = 4
    }
}
