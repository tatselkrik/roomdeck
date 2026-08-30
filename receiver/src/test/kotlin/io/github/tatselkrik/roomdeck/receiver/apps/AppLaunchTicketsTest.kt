package io.github.tatselkrik.roomdeck.receiver.apps

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class AppLaunchTicketsTest {
    @Test
    fun routeTargetsTheExactActivityAndIsConsumedOnce() {
        val target = AppLaunchTarget("com.example.video", "com.example.video.TvActivity")
        val route = AppLaunchTickets.issue(target)

        assertTrue(route.startsWith("roomdeck://app/"))
        val token = route.substringAfterLast('/')
        assertEquals(target, AppLaunchTickets.consume(token))
        assertNull(AppLaunchTickets.consume(token))
    }
}
