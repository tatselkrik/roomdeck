package io.github.tatselkrik.roomdeck

import io.github.tatselkrik.roomdeck.data.TvAppItem
import org.junit.Assert.assertEquals
import org.junit.Test

class AppOrderTest {
    private val apps = listOf(
        TvAppItem("Alpha", "alpha", null),
        TvAppItem("Bravo", "bravo", null),
        TvAppItem("Charlie", "charlie", null),
    )

    @Test
    fun appliesSavedPackagesAndAppendsNewApps() {
        val ordered = applySavedAppOrder(apps, listOf("charlie", "alpha", "removed"))
        assertEquals(listOf("charlie", "alpha", "bravo"), ordered.map(TvAppItem::packageName))
    }

    @Test
    fun movesAnAppToTheHoveredPosition() {
        val ordered = moveAppBefore(apps, "alpha", "charlie")
        assertEquals(listOf("bravo", "charlie", "alpha"), ordered.map(TvAppItem::packageName))
    }

    @Test
    fun resetUsesTheCatalogsAlphabeticalOrder() {
        val ordered = defaultAppOrder(apps.reversed())
        assertEquals(listOf("alpha", "bravo", "charlie"), ordered.map(TvAppItem::packageName))
    }
}
