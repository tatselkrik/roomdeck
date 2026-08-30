package io.github.tatselkrik.roomdeck

import io.github.tatselkrik.roomdeck.data.TvAppItem
import org.junit.Assert.assertEquals
import org.junit.Test

class AppIconLoadingTest {
    @Test
    fun mergesOnlyLoadedIconsAndPreservesCurrentOrder() {
        val current = listOf(
            TvAppItem("Charlie", "charlie", null),
            TvAppItem("Alpha", "alpha", null),
            TvAppItem("Bravo", "bravo", null),
        )

        val merged = mergeAppIcons(
            current,
            mapOf("bravo" to "encoded-bravo", "missing" to "ignored"),
        )

        assertEquals(listOf("charlie", "alpha", "bravo"), merged.map(TvAppItem::packageName))
        assertEquals(listOf(null, null, "encoded-bravo"), merged.map(TvAppItem::iconPngBase64))
    }
}
