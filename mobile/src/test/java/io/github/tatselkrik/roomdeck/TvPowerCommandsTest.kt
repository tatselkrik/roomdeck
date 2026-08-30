package io.github.tatselkrik.roomdeck

import io.github.tatselkrik.roomdeck.remote.AndroidTvKey
import org.junit.Assert.assertEquals
import org.junit.Test

class TvPowerCommandsTest {
    @Test
    fun `off uses the TCL-tested standard power key`() {
        assertEquals(AndroidTvKey.Power, TV_POWER_OFF_KEY)
    }
}
