package io.github.tatselkrik.roomdeck

import io.github.tatselkrik.roomdeck.data.TvAppItem

internal fun applySavedAppOrder(
    apps: List<TvAppItem>,
    savedPackages: List<String>,
): List<TvAppItem> {
    if (savedPackages.isEmpty()) return apps
    val byPackage = apps.associateBy(TvAppItem::packageName)
    val saved = savedPackages.distinct().mapNotNull(byPackage::get)
    val savedNames = saved.asSequence().map(TvAppItem::packageName).toHashSet()
    return saved + apps.filterNot { it.packageName in savedNames }
}

internal fun moveAppBefore(
    apps: List<TvAppItem>,
    draggedPackage: String,
    targetPackage: String,
): List<TvAppItem> {
    if (draggedPackage == targetPackage) return apps
    val fromIndex = apps.indexOfFirst { it.packageName == draggedPackage }
    val targetIndex = apps.indexOfFirst { it.packageName == targetPackage }
    if (fromIndex < 0 || targetIndex < 0) return apps
    return apps.toMutableList().apply {
        val dragged = removeAt(fromIndex)
        add(targetIndex.coerceAtMost(size), dragged)
    }
}

internal fun defaultAppOrder(apps: List<TvAppItem>): List<TvAppItem> =
    apps.sortedBy { it.label.lowercase() }
