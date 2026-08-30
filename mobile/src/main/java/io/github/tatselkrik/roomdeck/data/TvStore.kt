package io.github.tatselkrik.roomdeck.data

import android.content.Context
import io.github.tatselkrik.roomdeck.remote.AndroidTvDevice
import org.json.JSONArray
import org.json.JSONObject
import java.security.MessageDigest

class TvStore(context: Context) {
    private val preferences = context.applicationContext
        .getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)

    fun profiles(): List<TvProfile> {
        val source = preferences.getString(KEY_PROFILES, null) ?: return emptyList()
        return runCatching {
            val array = JSONArray(source)
            buildList {
                repeat(array.length()) { index ->
                    val json = array.getJSONObject(index)
                    add(
                        TvProfile(
                            id = json.getString("id"),
                            roomName = json.getString("roomName"),
                            deviceName = json.optString("deviceName", json.getString("roomName")),
                            host = json.getString("host"),
                            remotePort = json.optInt("remotePort", 6466),
                            pairingPort = json.optInt("pairingPort", 6467),
                            modelName = json.optString("modelName").takeIf(String::isNotBlank),
                            stableHardwareId = json.optString("stableHardwareId").takeIf(String::isNotBlank),
                            receiverToken = json.optString("receiverToken").takeIf(String::isNotBlank),
                            appOrder = json.optJSONArray("appOrder")?.let { order ->
                                buildList {
                                    repeat(order.length()) { orderIndex ->
                                        order.optString(orderIndex).takeIf(String::isNotBlank)?.let(::add)
                                    }
                                }
                            }.orEmpty(),
                        ),
                    )
                }
            }
        }.getOrDefault(emptyList())
    }

    @Synchronized
    fun save(profile: TvProfile) {
        val updated = profiles().filterNot { it.id == profile.id } + profile
        write(updated)
    }

    @Synchronized
    fun remove(profileId: String) {
        write(profiles().filterNot { it.id == profileId })
    }

    fun fromDevice(device: AndroidTvDevice, roomName: String): TvProfile {
        val stable = device.stableId?.takeIf(String::isNotBlank)
            ?: "${device.name}|${device.modelName.orEmpty()}|${device.host}"
        return TvProfile(
            id = sha256(stable).take(20),
            roomName = roomName.trim().ifBlank { device.name },
            deviceName = device.name,
            host = device.host,
            remotePort = device.port,
            pairingPort = device.pairingPort,
            modelName = device.modelName,
            stableHardwareId = device.stableId,
        )
    }

    private fun write(profiles: List<TvProfile>) {
        val array = JSONArray()
        profiles.sortedBy { it.roomName.lowercase() }.forEach { profile ->
            array.put(
                JSONObject()
                    .put("id", profile.id)
                    .put("roomName", profile.roomName)
                    .put("deviceName", profile.deviceName)
                    .put("host", profile.host)
                    .put("remotePort", profile.remotePort)
                    .put("pairingPort", profile.pairingPort)
                    .put("modelName", profile.modelName.orEmpty())
                    .put("stableHardwareId", profile.stableHardwareId.orEmpty())
                    .put("receiverToken", profile.receiverToken.orEmpty())
                    .put("appOrder", JSONArray().apply { profile.appOrder.forEach(::put) }),
            )
        }
        preferences.edit().putString(KEY_PROFILES, array.toString()).apply()
    }

    private fun sha256(value: String): String = MessageDigest.getInstance("SHA-256")
        .digest(value.toByteArray(Charsets.UTF_8))
        .joinToString("") { "%02x".format(it.toInt() and 0xff) }

    private companion object {
        const val PREFERENCES_NAME = "roomdeck_mobile"
        const val KEY_PROFILES = "tv_profiles"
    }
}
