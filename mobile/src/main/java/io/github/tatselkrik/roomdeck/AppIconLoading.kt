package io.github.tatselkrik.roomdeck

import io.github.tatselkrik.roomdeck.data.TvAppItem

internal fun mergeAppIcons(
    current: List<TvAppItem>,
    loadedIcons: Map<String, String?>,
): List<TvAppItem> = current.map { app ->
    if (loadedIcons.containsKey(app.packageName)) {
        app.copy(iconPngBase64 = loadedIcons[app.packageName])
    } else {
        app
    }
}
