package io.github.tatselkrik.roomdeck.receiver.pairing

import android.app.Activity
import android.os.Bundle

/** Approves Receiver access only when Android TV Remote launches the one-time route. */
class EnrollmentRouterActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        intent?.data?.lastPathSegment?.let(ReceiverEnrollment::approve)
        finish()
    }
}
