package io.github.tatselkrik.roomdeck.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val RoomDeckBackground = Color(0xFF08131F)
val RoomDeckSurface = Color(0xFF102332)
val RoomDeckSurfaceHigh = Color(0xFF173347)
val RoomDeckMint = Color(0xFF68E0CF)
val RoomDeckAmber = Color(0xFFFFB868)
val RoomDeckBlue = Color(0xFF8AA4FF)
val RoomDeckMuted = Color(0xFF9BB0BE)

private val RoomDeckColors = darkColorScheme(
    primary = RoomDeckMint,
    onPrimary = RoomDeckBackground,
    secondary = RoomDeckAmber,
    tertiary = RoomDeckBlue,
    background = RoomDeckBackground,
    onBackground = Color(0xFFF3F7F9),
    surface = RoomDeckSurface,
    onSurface = Color(0xFFF3F7F9),
    surfaceVariant = RoomDeckSurfaceHigh,
    onSurfaceVariant = RoomDeckMuted,
    error = Color(0xFFFF7D7D),
)

@Composable
fun RoomDeckTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = RoomDeckColors, content = content)
}
