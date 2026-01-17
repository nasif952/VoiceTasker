package com.voicetasker.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

/**
 * BroadcastReceiver for handling alarm broadcasts from AlarmManager.
 *
 * This receiver is triggered when a scheduled reminder alarm fires
 * and displays the notification to the user.
 */
class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        // TODO: Show reminder notification
        // This will be implemented in the reminder feature module
    }
}
