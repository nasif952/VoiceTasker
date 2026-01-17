package com.voicetasker.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

/**
 * BroadcastReceiver for handling device boot completion.
 *
 * This receiver is triggered when the device boots up to reschedule
 * any pending reminders that were set before the reboot.
 */
class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent?.action == Intent.ACTION_BOOT_COMPLETED) {
            // TODO: Reschedule all pending reminders
            // This will be implemented in the reminder feature module
        }
    }
}
