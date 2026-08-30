package io.github.tatselkrik.roomdeck.receiver

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.tv.material3.Button
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Surface
import androidx.tv.material3.Text
import io.github.tatselkrik.roomdeck.receiver.data.ReceiverPreferences
import io.github.tatselkrik.roomdeck.receiver.service.ReceiverRuntime
import io.github.tatselkrik.roomdeck.receiver.service.RoomDeckService

class MainActivity : ComponentActivity() {
    private var refreshCounter by mutableStateOf(0)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        RoomDeckService.start(this)
        setContent {
            val ignored = refreshCounter
            ReceiverScreen(
                preferences = remember(ignored) { ReceiverPreferences(this) },
                onResetAccess = {
                    ReceiverPreferences(this).resetPhoneAccess()
                    refreshCounter += 1
                },
                onRestart = { RoomDeckService.restart(this) },
            )
        }
    }
}

@Composable
private fun ReceiverScreen(
    preferences: ReceiverPreferences,
    onResetAccess: () -> Unit,
    onRestart: () -> Unit,
) {
    val runtime by ReceiverRuntime.status.collectAsStateWithLifecycle()
    MaterialTheme {
        Surface(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier.fillMaxSize().padding(horizontal = 64.dp, vertical = 44.dp),
                verticalArrangement = Arrangement.SpaceBetween,
            ) {
                Column {
                    Text(
                        text = "RoomDeck Receiver",
                        color = Color(0xFF68E0CF),
                        fontSize = 36.sp,
                        fontWeight = FontWeight.Bold,
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = "Lists and opens this TV's installed apps through Tailscale",
                        color = Color(0xFF9BB0BE),
                        fontSize = 18.sp,
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(24.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    InfoCard(
                        modifier = Modifier.weight(1f).height(170.dp),
                        label = "CONNECTION",
                        primary = when {
                            runtime.running -> "Ready"
                            runtime.error != null -> "Needs attention"
                            else -> "Waiting for Tailscale"
                        },
                        secondary = when {
                            runtime.error != null -> runtime.error.orEmpty()
                            runtime.address != null ->
                                "Tailscale address: ${runtime.address}\nEnter this address in RoomDeck on your phone."
                            else -> "Open Tailscale on this TV and connect to your tailnet"
                        },
                        accent = if (runtime.running) Color(0xFF68E0CF) else Color(0xFFFFB868),
                    )
                    InfoCard(
                        modifier = Modifier.weight(1f).height(170.dp),
                        label = "PHONE ACCESS",
                        primary = if (preferences.isPaired) "Connected" else "Waiting for phone",
                        secondary = if (preferences.isPaired) {
                            "RoomDeck Controller is authorized for this TV"
                        } else {
                            "Pair this TV in RoomDeck using the Android TV Remote code"
                        },
                        accent = Color(0xFFFFB868),
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = "Receiver ${BuildConfig.VERSION_NAME}",
                        color = Color(0xFF9BB0BE),
                        fontSize = 14.sp,
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(18.dp)) {
                        Button(onClick = onResetAccess) { Text("Reset phone access") }
                        Button(onClick = onRestart) { Text("Restart Receiver") }
                    }
                }
            }
        }
    }
}

@Composable
private fun InfoCard(
    modifier: Modifier,
    label: String,
    primary: String,
    secondary: String,
    accent: Color,
) {
    Column(
        modifier = modifier
            .background(Color(0xFF102332), androidx.compose.foundation.shape.RoundedCornerShape(22.dp))
            .padding(24.dp),
    ) {
        Text(label, color = accent, fontSize = 13.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(10.dp))
        Text(primary, color = Color.White, fontSize = 27.sp, fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.height(8.dp))
        Text(secondary, color = Color(0xFF9BB0BE), fontSize = 15.sp)
    }
}
