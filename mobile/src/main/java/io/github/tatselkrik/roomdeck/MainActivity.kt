package io.github.tatselkrik.roomdeck

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import io.github.tatselkrik.roomdeck.ui.RoomDeckApp
import io.github.tatselkrik.roomdeck.ui.RoomDeckTheme

class MainActivity : ComponentActivity() {
    private val viewModel by viewModels<RoomDeckViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            RoomDeckTheme {
                RoomDeckApp(viewModel)
            }
        }
    }
}
