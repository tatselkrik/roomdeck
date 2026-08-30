package io.github.tatselkrik.roomdeck.ui

import android.graphics.BitmapFactory
import android.util.Base64
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Apps
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.ErrorOutline
import androidx.compose.material.icons.rounded.FastForward
import androidx.compose.material.icons.rounded.FastRewind
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Input
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material.icons.rounded.KeyboardArrowLeft
import androidx.compose.material.icons.rounded.KeyboardArrowRight
import androidx.compose.material.icons.rounded.KeyboardArrowUp
import androidx.compose.material.icons.rounded.Link
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PowerSettingsNew
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.SkipNext
import androidx.compose.material.icons.rounded.SkipPrevious

import androidx.compose.material.icons.rounded.Tv
import androidx.compose.material.icons.rounded.VolumeOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults

import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.tatselkrik.roomdeck.PairingPrompt
import io.github.tatselkrik.roomdeck.ReceiverLinkState
import io.github.tatselkrik.roomdeck.RoomDeckTab
import io.github.tatselkrik.roomdeck.RoomDeckViewModel
import io.github.tatselkrik.roomdeck.data.TvAppItem

import io.github.tatselkrik.roomdeck.data.TvProfile

import io.github.tatselkrik.roomdeck.remote.AndroidTvKey
import io.github.tatselkrik.roomdeck.remote.AndroidTvState

@Composable
fun RoomDeckApp(viewModel: RoomDeckViewModel) {
    val profiles by viewModel.profiles.collectAsStateWithLifecycle()

    val selectedId by viewModel.selectedProfileId.collectAsStateWithLifecycle()
    val tab by viewModel.tab.collectAsStateWithLifecycle()
    val states by viewModel.remoteStates.collectAsStateWithLifecycle()
    val receiverStates by viewModel.receiverStates.collectAsStateWithLifecycle()
    val prompt by viewModel.pairingPrompt.collectAsStateWithLifecycle()
    val apps by viewModel.apps.collectAsStateWithLifecycle()

    val busy by viewModel.busy.collectAsStateWithLifecycle()
    val notice by viewModel.notice.collectAsStateWithLifecycle()

    var showAdd by rememberSaveable { mutableStateOf(false) }
    val snackbar = remember { SnackbarHostState() }


    LaunchedEffect(notice) {
        notice?.let {
            snackbar.showSnackbar(
                message = it,
                duration = SnackbarDuration.Long,
                withDismissAction = true,
            )
            viewModel.clearNotice()
        }
    }

    Scaffold(
        containerColor = RoomDeckBackground,
        snackbarHost = { SnackbarHost(snackbar) },
    ) { padding ->
        Box(Modifier.fillMaxSize().padding(padding)) {
            when {
                showAdd -> AddTvScreen(
                    onBack = { showAdd = false },
                    onManualPair = { host, room ->
                        showAdd = false
                        viewModel.beginManualPairing(host, room)
                    },
                )
                selectedId != null -> {
                    val profile = profiles.firstOrNull { it.id == selectedId }
                    if (profile == null) {
                        HomeScreen(
                            profiles = profiles,
                            states = states,
                            onSelect = viewModel::selectProfile,
                            onAdd = { showAdd = true },
                            onAllOff = viewModel::allPowerOff,
                        )
                    } else {
                        TvDetailScreen(
                            profile = profile,
                            state = states[profile.id] ?: AndroidTvState.Idle,
                            receiverState = receiverStates[profile.id] ?: ReceiverLinkState.IDLE,
                            tab = tab,
                            apps = apps,
                            onBack = { viewModel.selectProfile(null) },
                            onTab = viewModel::setTab,
                            onKey = viewModel::sendKey,
                            onPowerOff = viewModel::turnSelectedOff,
                            onRefresh = viewModel::refreshSelected,
                            onConnectReceiver = { viewModel.beginReceiverPairing(profile.id) },
                            onLaunchApp = viewModel::launchApp,
                            onMoveApp = viewModel::moveApp,
                            onResetAppOrder = viewModel::resetAppOrder,
                            onRemove = viewModel::removeSelectedProfile,
                        )
                    }
                }
                else -> HomeScreen(
                    profiles = profiles,
                    states = states,
                    onSelect = viewModel::selectProfile,
                    onAdd = { showAdd = true },
                    onAllOff = viewModel::allPowerOff,
                )
            }

            AnimatedVisibility(
                visible = busy != null,
                modifier = Modifier.align(Alignment.TopCenter),
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(RoomDeckSurfaceHigh)
                        .padding(top = 8.dp),
                ) {
                    LinearProgressIndicator(Modifier.fillMaxWidth())
                    Text(
                        text = busy.orEmpty(),
                        modifier = Modifier.padding(horizontal = 18.dp, vertical = 8.dp),
                        color = RoomDeckMuted,
                        fontSize = 13.sp,
                    )
                }
            }
        }
    }

    prompt?.let {
        PairingCodeDialog(
            prompt = it,
            onSubmit = viewModel::submitPairingCode,
            onCancel = viewModel::cancelPairing,
        )
    }
}

@Composable
private fun HomeScreen(
    profiles: List<TvProfile>,
    states: Map<String, AndroidTvState>,
    onSelect: (String) -> Unit,
    onAdd: () -> Unit,
    onAllOff: () -> Unit,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 22.dp, end = 22.dp, top = 54.dp, bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp),
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column {
                    Text(
                        "RoomDeck",
                        fontSize = 34.sp,
                        fontWeight = FontWeight.Bold,
                        color = RoomDeckMint,
                    )
                    Text(
                        "Your TVs. One control deck.",
                        color = RoomDeckMuted,
                        fontSize = 15.sp,
                    )
                }
                FilledTonalIconButton(onClick = onAdd) {
                    Icon(Icons.Rounded.Add, contentDescription = "Add TV")
                }
            }
        }
        item {
            OutlinedButton(
                onClick = onAllOff,
                enabled = profiles.isNotEmpty(),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Icon(Icons.Rounded.PowerSettingsNew, null)
                Spacer(Modifier.width(8.dp))
                Text("All TVs Off", maxLines = 1, softWrap = false)
            }
        }
        if (profiles.isEmpty()) {
            item {
                EmptyDeckCard(onAdd)
            }
        } else {
            item {
                Text(
                    "ROOMS",
                    color = RoomDeckMuted,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                )
            }
            items(profiles, key = TvProfile::id) { profile ->
                TvProfileCard(
                    profile = profile,
                    state = states[profile.id] ?: AndroidTvState.Idle,
                    onClick = { onSelect(profile.id) },
                )
            }
        }
    }
}

@Composable
private fun EmptyDeckCard(onAdd: () -> Unit) {
    ElevatedCard(
        colors = CardDefaults.elevatedCardColors(containerColor = RoomDeckSurface),
        shape = RoundedCornerShape(24.dp),
    ) {
        Column(Modifier.padding(26.dp)) {
            Icon(Icons.Rounded.Tv, null, tint = RoomDeckBlue, modifier = Modifier.size(42.dp))
            Spacer(Modifier.height(16.dp))
            Text("Add your first TCL TV", fontWeight = FontWeight.SemiBold, fontSize = 22.sp)
            Spacer(Modifier.height(8.dp))
            Text(
                "Open RoomDeck Receiver on the TV, then enter its Tailscale address.",
                color = RoomDeckMuted,
            )
            Spacer(Modifier.height(20.dp))
            Button(onClick = onAdd) { Text("Add a TV") }
        }
    }
}

@Composable
private fun TvProfileCard(profile: TvProfile, state: AndroidTvState, onClick: () -> Unit) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        colors = CardDefaults.elevatedCardColors(containerColor = RoomDeckSurface),
        shape = RoundedCornerShape(22.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(20.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier.size(54.dp).clip(RoundedCornerShape(16.dp)).background(RoomDeckSurfaceHigh),
                contentAlignment = Alignment.Center,
            ) {
                Icon(Icons.Rounded.Tv, null, tint = RoomDeckMint)
            }
            Spacer(Modifier.width(16.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    profile.roomName,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    softWrap = false,
                    overflow = TextOverflow.Ellipsis,
                )
                val deviceLabel = profile.modelName ?: profile.deviceName
                if (!deviceLabel.equals(profile.roomName, ignoreCase = true)) {
                    Text(
                        deviceLabel,
                        color = RoomDeckMuted,
                        fontSize = 13.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
            StatusPill(state)
            Spacer(Modifier.width(8.dp))
            Icon(Icons.Rounded.KeyboardArrowRight, null, tint = RoomDeckMuted)
        }
    }
}

@Composable
private fun CompactStatusIcon(state: AndroidTvState) {
    val (label, icon, color) = when (state) {
        AndroidTvState.Active -> Triple("Connected", Icons.Rounded.CheckCircle, RoomDeckMint)
        AndroidTvState.Connecting -> Triple("Connecting", Icons.Rounded.Tv, RoomDeckAmber)
        is AndroidTvState.Reconnecting -> Triple("Reconnecting", Icons.Rounded.Tv, RoomDeckAmber)
        is AndroidTvState.Error -> Triple("Offline", Icons.Rounded.ErrorOutline, MaterialTheme.colorScheme.error)
        AndroidTvState.Idle -> Triple("Standby", Icons.Rounded.PowerSettingsNew, RoomDeckMuted)
    }
    Icon(icon, contentDescription = label, tint = color, modifier = Modifier.size(24.dp))
}

@Composable
private fun StatusPill(state: AndroidTvState) {
    val (label, color) = when (state) {
        AndroidTvState.Active -> "Connected" to RoomDeckMint
        AndroidTvState.Connecting -> "Connecting" to RoomDeckAmber
        is AndroidTvState.Reconnecting -> "Reconnecting" to RoomDeckAmber
        is AndroidTvState.Error -> "Offline" to MaterialTheme.colorScheme.error
        AndroidTvState.Idle -> "Standby" to RoomDeckMuted
    }
    Row(
        modifier = Modifier.clip(CircleShape).background(color.copy(alpha = 0.12f)).padding(horizontal = 10.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(Modifier.size(7.dp).clip(CircleShape).background(color))
        Spacer(Modifier.width(6.dp))
        Text(label, color = color, fontSize = 12.sp, fontWeight = FontWeight.Medium)
    }
}

@Composable
private fun AddTvScreen(
    onBack: () -> Unit,
    onManualPair: (String, String) -> Unit,
) {
    var roomName by rememberSaveable { mutableStateOf("") }
    var host by rememberSaveable { mutableStateOf("") }
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 22.dp, end = 22.dp, top = 48.dp, bottom = 36.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack) { Icon(Icons.Rounded.ArrowBack, "Back") }
                Spacer(Modifier.width(8.dp))
                Column {
                    Text("Add a TV", fontSize = 28.sp, fontWeight = FontWeight.Bold)
                    Text(
                        "Connect through your private Tailscale network.",
                        color = RoomDeckMuted,
                    )
                }
            }
        }
        item {
            Card(colors = CardDefaults.cardColors(containerColor = RoomDeckSurface)) {
                Column(Modifier.fillMaxWidth().padding(20.dp)) {
                    Text("BEFORE PAIRING", color = RoomDeckMint, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "Connect Tailscale on this phone and the TV. Open RoomDeck Receiver on the TV and copy the 100.x address shown there.",
                        color = RoomDeckMuted,
                    )
                }
            }
        }
        item {
            OutlinedTextField(
                value = roomName,
                onValueChange = { roomName = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Room name") },
                placeholder = { Text("Living Room or Bedroom") },
                singleLine = true,
            )
        }
        item {
            OutlinedTextField(
                value = host,
                onValueChange = { host = it.filterNot(Char::isWhitespace) },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("TV Tailscale address") },
                placeholder = { Text("100.x.x.x") },
                singleLine = true,
            )
        }
        item {
            Button(
                onClick = { onManualPair(host, roomName) },
                enabled = host.isNotBlank() && roomName.isNotBlank(),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Icon(Icons.Rounded.Link, null)
                Spacer(Modifier.width(8.dp))
                Text("Pair TV")
            }
        }
    }
}

@Composable
private fun TvDetailScreen(
    profile: TvProfile,
    state: AndroidTvState,
    receiverState: ReceiverLinkState,
    tab: RoomDeckTab,
    apps: List<TvAppItem>,
    onBack: () -> Unit,
    onTab: (RoomDeckTab) -> Unit,
    onKey: (AndroidTvKey) -> Unit,
    onPowerOff: () -> Unit,
    onRefresh: () -> Unit,
    onConnectReceiver: () -> Unit,
    onLaunchApp: (TvAppItem) -> Unit,
    onMoveApp: (String, String) -> Unit,
    onResetAppOrder: () -> Unit,
    onRemove: () -> Unit,
) {
    var confirmRemove by remember { mutableStateOf(false) }
    Column(Modifier.fillMaxSize()) {
        Column(Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack) { Icon(Icons.Rounded.ArrowBack, "Back") }
                Text(
                    profile.roomName,
                    modifier = Modifier.weight(1f).padding(horizontal = 8.dp),
                    fontSize = 25.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    softWrap = false,
                    overflow = TextOverflow.Ellipsis,
                )
                CompactStatusIcon(state)
                IconButton(onClick = onRefresh) { Icon(Icons.Rounded.Refresh, "Refresh") }
                IconButton(onClick = onPowerOff) {
                    Icon(Icons.Rounded.PowerSettingsNew, "Turn TV off")
                }
            }

        }
        TabRow(selectedTabIndex = tab.ordinal, containerColor = RoomDeckBackground) {
            RoomDeckTab.entries.forEach { candidate ->
                Tab(
                    selected = tab == candidate,
                    onClick = { onTab(candidate) },
                    text = {
                        Text(
                            when (candidate) {
                                RoomDeckTab.REMOTE -> "Remote"

                                RoomDeckTab.APPS -> "Apps"
                            },
                            maxLines = 1,
                            softWrap = false,
                            fontSize = 11.sp,
                        )
                    },
                    icon = {
                        Icon(
                            when (candidate) {
                                RoomDeckTab.REMOTE -> Icons.Rounded.Tv

                                RoomDeckTab.APPS -> Icons.Rounded.Apps
                            },
                            null,
                        )
                    },
                )
            }
        }
        Box(Modifier.fillMaxWidth().weight(1f)) {
            when (tab) {
                RoomDeckTab.REMOTE -> RemotePanel(onKey, onRemove = { confirmRemove = true })

                RoomDeckTab.APPS -> AppsPanel(
                    receiverState = receiverState,
                    apps = apps,
                    onConnectReceiver = onConnectReceiver,
                    onLaunch = onLaunchApp,
                    onRefresh = onRefresh,
                    onMoveApp = onMoveApp,
                    onResetOrder = onResetAppOrder,
                )
            }
        }
    }
    if (confirmRemove) {
        AlertDialog(
            onDismissRequest = { confirmRemove = false },
            title = { Text("Remove ${profile.roomName}?") },
            text = { Text("This removes its local pairing from RoomDeck. It does not change the TV itself.") },
            confirmButton = {
                TextButton(onClick = {
                    confirmRemove = false
                    onRemove()
                }) { Text("Remove") }
            },
            dismissButton = {
                TextButton(onClick = { confirmRemove = false }) { Text("Cancel") }
            },
        )
    }
}

@Composable
private fun RemotePanel(onKey: (AndroidTvKey) -> Unit, onRemove: () -> Unit) {
    BoxWithConstraints(Modifier.fillMaxSize()) {
        if (maxWidth > maxHeight) {
            LandscapeRemotePanel(onKey = onKey, onRemove = onRemove, availableHeight = maxHeight)
        } else {
            PortraitRemotePanel(onKey = onKey, onRemove = onRemove, availableHeight = maxHeight)
        }
    }
}

@Composable
private fun PortraitRemotePanel(
    onKey: (AndroidTvKey) -> Unit,
    onRemove: () -> Unit,
    availableHeight: Dp,
) {
    val compact = availableHeight < 520.dp
    val dpadSize = if (compact) 56.dp else 66.dp
    val railSize = if (compact) 42.dp else 46.dp
    val navHeight = if (compact) 50.dp else 54.dp
    Column(
        modifier = Modifier.fillMaxSize().padding(horizontal = 12.dp, vertical = if (compact) 6.dp else 10.dp),
        verticalArrangement = Arrangement.SpaceBetween,
    ) {
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            RemoteIconTonalButton(Icons.Rounded.ArrowBack, "Back", Modifier.weight(1f), navHeight) {
                onKey(AndroidTvKey.Back)
            }
            RemoteIconTonalButton(Icons.Rounded.Home, "Home", Modifier.weight(1f), navHeight) {
                onKey(AndroidTvKey.Home)
            }
            RemoteIconTonalButton(Icons.Rounded.Input, "Input", Modifier.weight(1f), navHeight) {
                onKey(AndroidTvKey.TvInput)
            }
        }
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center,
        ) {
            DpadCluster(
                buttonSize = dpadSize,
                onKey = onKey,
                buttonSpacing = if (compact) 4.dp else 5.dp,
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = if (compact) 24.dp else 34.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top,
        ) {
            VolumeRail(railSize, showLabel = false, onKey = onKey)
            ChannelRail(railSize, showLabel = false, onKey = onKey)
        }
        PlaybackStrip(
            modifier = Modifier.fillMaxWidth(),
            buttonHeight = if (compact) 42.dp else 46.dp,
            onKey = onKey,
        )
        TextButton(
            onClick = onRemove,
            modifier = Modifier.fillMaxWidth().height(if (compact) 36.dp else 40.dp),
        ) {
            Icon(Icons.Rounded.DeleteOutline, null)
            Spacer(Modifier.width(8.dp))
            Text("Remove this TV")
        }
    }
}

@Composable
private fun LandscapeRemotePanel(
    onKey: (AndroidTvKey) -> Unit,
    onRemove: () -> Unit,
    availableHeight: Dp,
) {
    val playbackHeight = 38.dp
    val removeHeight = 44.dp
    val footerHeight = playbackHeight + removeHeight
    val controlSize = ((availableHeight - footerHeight - 20.dp) / 3).coerceIn(28.dp, 38.dp)
    val controlRowHeight = controlSize * 3 + 4.dp
    Column(
        modifier = Modifier.fillMaxSize().padding(horizontal = 8.dp),
        verticalArrangement = Arrangement.SpaceBetween,
    ) {
        Spacer(Modifier.height(0.dp))
        Row(
            modifier = Modifier.fillMaxWidth().height(controlRowHeight),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            NavigationRail(controlSize, onKey)
            VolumeRail(controlSize, showLabel = false, onKey = onKey)
            DpadCluster(controlSize, onKey, buttonSpacing = 2.dp)
            ChannelRail(controlSize, showLabel = false, onKey = onKey)
        }
        PlaybackStrip(
            modifier = Modifier.fillMaxWidth().height(playbackHeight),
            buttonHeight = playbackHeight - 2.dp,
            onKey = onKey,
        )
        TextButton(
            onClick = onRemove,
            modifier = Modifier.fillMaxWidth().height(removeHeight),
            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp),
        ) {
            Icon(Icons.Rounded.DeleteOutline, null)
            Spacer(Modifier.width(6.dp))
            Text("Remove this TV", maxLines = 1, softWrap = false)
        }
    }
}

@Composable
private fun NavigationRail(buttonSize: Dp, onKey: (AndroidTvKey) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        RailIconButton(Icons.Rounded.ArrowBack, "Back", buttonSize) { onKey(AndroidTvKey.Back) }
        RailIconButton(Icons.Rounded.Home, "Home", buttonSize) { onKey(AndroidTvKey.Home) }
        RailIconButton(Icons.Rounded.Input, "Input", buttonSize) { onKey(AndroidTvKey.TvInput) }
    }
}

@Composable
private fun VolumeRail(buttonSize: Dp, showLabel: Boolean, onKey: (AndroidTvKey) -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        if (showLabel) Text("VOL", color = RoomDeckMuted, fontSize = 9.sp, fontWeight = FontWeight.Bold)
        RailTextButton("+", "Volume up", buttonSize) { onKey(AndroidTvKey.VolumeUp) }
        RailIconButton(Icons.Rounded.VolumeOff, "Mute", buttonSize) { onKey(AndroidTvKey.Mute) }
        RailTextButton("−", "Volume down", buttonSize) { onKey(AndroidTvKey.VolumeDown) }
    }
}

@Composable
private fun ChannelRail(buttonSize: Dp, showLabel: Boolean, onKey: (AndroidTvKey) -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        if (showLabel) Text("CH", color = RoomDeckMuted, fontSize = 9.sp, fontWeight = FontWeight.Bold)
        RailIconButton(Icons.Rounded.KeyboardArrowUp, "Channel up", buttonSize) {
            onKey(AndroidTvKey.ChannelUp)
        }
        Spacer(Modifier.size(buttonSize))
        RailIconButton(Icons.Rounded.KeyboardArrowDown, "Channel down", buttonSize) {
            onKey(AndroidTvKey.ChannelDown)
        }
    }
}

@Composable
private fun RailTextButton(label: String, description: String, size: Dp, onClick: () -> Unit) {
    FilledTonalIconButton(onClick = onClick, modifier = Modifier.size(size)) {
        Text(label, fontSize = 20.sp, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun RailIconButton(icon: ImageVector, description: String, size: Dp, onClick: () -> Unit) {
    FilledTonalIconButton(onClick = onClick, modifier = Modifier.size(size)) {
        Icon(icon, description, modifier = Modifier.size((size * 0.48f).coerceAtLeast(18.dp)))
    }
}

@Composable
private fun DpadCluster(
    buttonSize: Dp,
    onKey: (AndroidTvKey) -> Unit,
    buttonSpacing: Dp = 0.dp,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(buttonSpacing),
    ) {
        DpadButton(Icons.Rounded.KeyboardArrowUp, "Up", buttonSize) { onKey(AndroidTvKey.DPadUp) }
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(buttonSpacing),
        ) {
            DpadButton(Icons.Rounded.KeyboardArrowLeft, "Left", buttonSize) { onKey(AndroidTvKey.DPadLeft) }
            FilledTonalIconButton(
                onClick = { onKey(AndroidTvKey.DPadCenter) },
                modifier = Modifier.size(buttonSize),
            ) { Text("OK", fontWeight = FontWeight.Bold, fontSize = if (buttonSize < 42.dp) 11.sp else 14.sp) }
            DpadButton(Icons.Rounded.KeyboardArrowRight, "Right", buttonSize) { onKey(AndroidTvKey.DPadRight) }
        }
        DpadButton(Icons.Rounded.KeyboardArrowDown, "Down", buttonSize) { onKey(AndroidTvKey.DPadDown) }
    }
}

@Composable
private fun PlaybackStrip(
    modifier: Modifier,
    buttonHeight: Dp,
    onKey: (AndroidTvKey) -> Unit,
) {
    Row(
        modifier = modifier
            .background(RoomDeckSurface, RoundedCornerShape(16.dp))
            .padding(horizontal = 8.dp, vertical = 1.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        RemoteIconButton(Icons.Rounded.SkipPrevious, "Previous", buttonHeight) { onKey(AndroidTvKey.MediaPrevious) }
        RemoteIconButton(Icons.Rounded.FastRewind, "Rewind", buttonHeight) { onKey(AndroidTvKey.MediaRewind) }
        RemoteIconButton(Icons.Rounded.Pause, "Play or pause", buttonHeight) { onKey(AndroidTvKey.DPadCenter) }
        RemoteIconButton(Icons.Rounded.FastForward, "Fast forward", buttonHeight) { onKey(AndroidTvKey.MediaFastForward) }
        RemoteIconButton(Icons.Rounded.SkipNext, "Next", buttonHeight) { onKey(AndroidTvKey.MediaNext) }
    }
}

@Composable
private fun DpadButton(icon: ImageVector, description: String, size: Dp, onClick: () -> Unit) {
    FilledTonalIconButton(onClick = onClick, modifier = Modifier.size(size)) {
        Icon(icon, description, modifier = Modifier.size((size * 0.55f).coerceAtLeast(18.dp)))
    }
}

@Composable
private fun RowScope.RemoteIconButton(
    icon: ImageVector,
    description: String,
    height: Dp,
    onClick: () -> Unit,
) {
    IconButton(onClick = onClick, modifier = Modifier.weight(1f).height(height)) {
        Icon(icon, description)
    }
}

@Composable
private fun RemoteIconTonalButton(
    icon: ImageVector,
    description: String,
    modifier: Modifier = Modifier,
    height: Dp = 54.dp,
    onClick: () -> Unit,
) {
    FilledTonalIconButton(onClick = onClick, modifier = modifier.height(height)) {
        Icon(icon, description, modifier = Modifier.size(24.dp))
    }
}
@Composable
private fun AppsPanel(
    receiverState: ReceiverLinkState,
    apps: List<TvAppItem>,
    onConnectReceiver: () -> Unit,
    onLaunch: (TvAppItem) -> Unit,
    onRefresh: () -> Unit,
    onMoveApp: (String, String) -> Unit,
    onResetOrder: () -> Unit,
) {
    if (apps.isEmpty() && receiverState == ReceiverLinkState.CONNECTING) {
        EmptyFeatureState(
            icon = Icons.Rounded.Link,
            title = "Connecting Receiver",
            message = "RoomDeck is connecting automatically and loading this TV's installed apps.",
            action = "Retry now",
            onAction = onConnectReceiver,
        )
        return
    }
    if (apps.isEmpty() && receiverState != ReceiverLinkState.CONNECTED) {
        ReceiverSetupCard("installed apps", onConnectReceiver)
        return
    }
    if (apps.isEmpty()) {
        EmptyFeatureState(
            icon = Icons.Rounded.Apps,
            title = "No apps loaded",
            message = "Make sure RoomDeck Receiver is open on this TV, then refresh.",
            action = "Refresh",
            onAction = onRefresh,
        )
        return
    }

    var draggingPackage by remember { mutableStateOf<String?>(null) }
    var lastTargetPackage by remember { mutableStateOf<String?>(null) }
    var dragPosition by remember { mutableStateOf(Offset.Zero) }
    val appBounds = remember { mutableStateMapOf<String, Rect>() }
    val currentPackages = apps.mapTo(mutableSetOf(), TvAppItem::packageName)

    Column(Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(start = 12.dp, end = 6.dp, top = 2.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                "Hold and drag to rearrange",
                modifier = Modifier.weight(1f),
                color = RoomDeckMuted,
                fontSize = 11.sp,
            )
            TextButton(onClick = onResetOrder) { Text("Reset order", fontSize = 11.sp) }
        }
        LazyVerticalGrid(
            columns = GridCells.Adaptive(78.dp),
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(start = 10.dp, end = 10.dp, top = 2.dp, bottom = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            items(apps, key = TvAppItem::packageName) { app ->
                val isDragging = draggingPackage == app.packageName
                Box(
                    modifier = Modifier
                        .height(88.dp)
                        .onGloballyPositioned { appBounds[app.packageName] = it.boundsInRoot() }
                        .pointerInput(app.packageName) {
                            detectDragGesturesAfterLongPress(
                                onDragStart = { localOffset ->
                                    draggingPackage = app.packageName
                                    lastTargetPackage = null
                                    dragPosition = appBounds[app.packageName]
                                        ?.topLeft
                                        ?.plus(localOffset)
                                        ?: localOffset
                                },
                                onDrag = { change, dragAmount ->
                                    change.consume()
                                    dragPosition += dragAmount
                                    val dragged = draggingPackage
                                    val target = appBounds.entries.firstOrNull { (packageName, bounds) ->
                                        packageName != dragged &&
                                            packageName in currentPackages &&
                                            bounds.contains(dragPosition)
                                    }?.key
                                    if (target != lastTargetPackage) {
                                        lastTargetPackage = target
                                        if (dragged != null && target != null) onMoveApp(dragged, target)
                                    }
                                },
                                onDragEnd = {
                                    draggingPackage = null
                                    lastTargetPackage = null
                                },
                                onDragCancel = {
                                    draggingPackage = null
                                    lastTargetPackage = null
                                },
                            )
                        }
                        .graphicsLayer {
                            alpha = if (isDragging) 0.72f else 1f
                            scaleX = if (isDragging) 1.06f else 1f
                            scaleY = if (isDragging) 1.06f else 1f
                        }
                        .clip(RoundedCornerShape(12.dp))
                        .clickable(enabled = !isDragging) { onLaunch(app) }
                        .padding(horizontal = 3.dp, vertical = 2.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                    ) {
                        Box(
                            Modifier.size(56.dp).clip(CircleShape).background(RoomDeckSurfaceHigh),
                            contentAlignment = Alignment.Center,
                        ) {
                            val appIcon = remember(app.iconPngBase64) {
                                app.iconPngBase64?.let { encoded ->
                                    runCatching {
                                        val bytes = Base64.decode(encoded, Base64.DEFAULT)
                                        BitmapFactory.decodeByteArray(bytes, 0, bytes.size)?.asImageBitmap()
                                    }.getOrNull()
                                }
                            }
                            if (appIcon != null) {
                                Image(
                                    appIcon,
                                    contentDescription = null,
                                    modifier = Modifier.fillMaxSize().padding(2.dp).clip(CircleShape),
                                )
                            } else {
                                Text(
                                    app.label.take(1).uppercase(),
                                    color = RoomDeckMint,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 18.sp,
                                )
                            }
                        }
                        Spacer(Modifier.height(4.dp))
                        Text(
                            app.label,
                            modifier = Modifier.fillMaxWidth(),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            maxLines = 1,
                            softWrap = false,
                            overflow = TextOverflow.Ellipsis,
                            textAlign = TextAlign.Center,
                        )
                    }
                }
            }
        }
    }
}
@Composable
private fun ReceiverSetupCard(feature: String, onConnectReceiver: () -> Unit) {
    Box(Modifier.fillMaxSize().padding(22.dp), contentAlignment = Alignment.Center) {
        ElevatedCard(
            colors = CardDefaults.elevatedCardColors(containerColor = RoomDeckSurface),
            shape = RoundedCornerShape(24.dp),
        ) {
            Column(Modifier.padding(26.dp), horizontalAlignment = Alignment.Start) {
                Icon(Icons.Rounded.Link, null, tint = RoomDeckAmber, modifier = Modifier.size(40.dp))
                Spacer(Modifier.height(14.dp))
                Text("RoomDeck Receiver unavailable", fontSize = 21.sp, fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.height(8.dp))
                Text(
                    "RoomDeck could not currently reach Receiver for $feature. Keep Receiver and Tailscale running on the TV, then retry.",
                    color = RoomDeckMuted,
                )
                Spacer(Modifier.height(18.dp))
                Button(onClick = onConnectReceiver) { Text("Retry Receiver") }
            }
        }
    }
}

@Composable
private fun EmptyFeatureState(
    icon: ImageVector,
    title: String,
    message: String,
    action: String,
    onAction: () -> Unit,
) {
    Box(Modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(icon, null, tint = RoomDeckMuted, modifier = Modifier.size(46.dp))
            Spacer(Modifier.height(14.dp))
            Text(title, fontSize = 20.sp, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(8.dp))
            Text(message, color = RoomDeckMuted)
            Spacer(Modifier.height(18.dp))
            OutlinedButton(onClick = onAction) { Text(action) }
        }
    }
}

@Composable
private fun PairingCodeDialog(
    prompt: PairingPrompt,
    onSubmit: (String) -> Unit,
    onCancel: () -> Unit,
) {
    var code by remember(prompt.title) { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onCancel,
        icon = {
            Icon(
                if (prompt.acceptsCode) Icons.Rounded.Link else Icons.Rounded.Tv,
                null,
            )
        },
        title = { Text(prompt.title) },
        text = {
            Column {
                Text(prompt.instruction)
                Spacer(Modifier.height(16.dp))
                OutlinedTextField(
                    value = code,
                    onValueChange = {
                        code = it.uppercase().filter(Char::isLetterOrDigit).take(6)
                    },
                    enabled = prompt.acceptsCode,
                    singleLine = true,
                    label = { Text("Six-character code") },
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onSubmit(code) },
                enabled = prompt.acceptsCode && code.length == 6,
            ) { Text("Pair") }
        },
        dismissButton = {
            TextButton(onClick = onCancel) { Text("Cancel") }
        },
    )
}
