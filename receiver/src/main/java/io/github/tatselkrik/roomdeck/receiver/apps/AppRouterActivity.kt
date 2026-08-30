package io.github.tatselkrik.roomdeck.receiver.apps

import android.app.Activity
import android.content.ComponentName
import android.content.Intent
import android.os.Bundle

/** Opens the selected installed TV app from a foreground Android TV app-link route. */
class AppRouterActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val token = intent?.data?.lastPathSegment.orEmpty()
        val target = AppLaunchTickets.consume(token)
        if (target != null) {
            val launchIntent = Intent(Intent.ACTION_MAIN)
                .addCategory(Intent.CATEGORY_LEANBACK_LAUNCHER)
                .setComponent(ComponentName(target.packageName, target.activityName))
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
            runCatching { startActivity(launchIntent) }
        }
        finish()
    }
}
