package io.github.tatselkrik.roomdeck.receiver.apps

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.graphics.Bitmap
import android.graphics.Canvas
import android.os.Build
import android.util.Base64
import java.io.ByteArrayOutputStream

data class LaunchableTvApp(
    val label: String,
    val packageName: String,
    val activityName: String,
)

class AppCatalog(private val context: Context) {
    private val packageManager = context.packageManager

    fun list(): List<LaunchableTvApp> =
        resolvedApps()
            .mapNotNull(::toLaunchableApp)
            .distinctBy { it.packageName }
            .sortedBy { it.label.lowercase() }

    fun find(packageName: String): LaunchableTvApp? =
        resolvedApps()
            .asSequence()
            .mapNotNull(::toLaunchableApp)
            .firstOrNull { it.packageName == packageName }

    fun icon(packageName: String): String? {
        val info = resolvedApps().firstOrNull { info ->
            info.activityInfo?.packageName == packageName && packageName != context.packageName
        } ?: return null
        return encodeIcon(info.loadIcon(packageManager))
    }

    private fun resolvedApps(): List<ResolveInfo> {
        val intent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LEANBACK_LAUNCHER)
        return if (Build.VERSION.SDK_INT >= 33) {
            packageManager.queryIntentActivities(
                intent,
                PackageManager.ResolveInfoFlags.of(PackageManager.MATCH_ALL.toLong()),
            )
        } else {
            @Suppress("DEPRECATION")
            packageManager.queryIntentActivities(intent, PackageManager.MATCH_ALL)
        }
    }

    private fun toLaunchableApp(info: ResolveInfo): LaunchableTvApp? {
        val activity = info.activityInfo ?: return null
        if (activity.packageName == context.packageName) return null
        return LaunchableTvApp(
            label = info.loadLabel(packageManager).toString().ifBlank { activity.packageName },
            packageName = activity.packageName,
            activityName = activity.name,
        )
    }

    private fun encodeIcon(drawable: android.graphics.drawable.Drawable): String? = runCatching {
        val bitmap = Bitmap.createBitmap(ICON_SIZE_PX, ICON_SIZE_PX, Bitmap.Config.ARGB_8888)
        drawable.mutate().apply {
            setBounds(0, 0, ICON_SIZE_PX, ICON_SIZE_PX)
            draw(Canvas(bitmap))
        }
        ByteArrayOutputStream().use { output ->
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, output)
            Base64.encodeToString(output.toByteArray(), Base64.NO_WRAP)
        }
    }.getOrNull()

    private companion object {
        const val ICON_SIZE_PX = 72
    }
}
