package com.mlab.network.service.service


import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager

class WorkRestartReceiver : BroadcastReceiver() {

    companion object {
        const val TAG = "WorkRestartReceiver"
    }

    override fun onReceive(context: Context, intent: Intent) {
        Log.d(TAG, "onReceive called")

        // We are starting NetworkObserverService via a worker and not directly because since Android 7
        // (but officially since Lollipop!), any process called by a BroadcastReceiver
        // (only manifest-declared receiver) is run at low priority and hence eventually
        // killed by Android.
        val startServiceRequest: OneTimeWorkRequest =
            OneTimeWorkRequest
                .Builder(ServiceRestartWorker::class.java)
                .build()

        WorkManager.getInstance(context)
            .enqueue(startServiceRequest)
    }
}