package com.mlab.network.service.service

import android.content.Context
import android.util.Log
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequest
import androidx.work.WorkManager
import androidx.work.Worker
import androidx.work.WorkerParameters
import java.util.concurrent.TimeUnit


class ServiceRestartWorker(
    private val context: Context,
    params: WorkerParameters
) : Worker(context, params) {

    companion object {
        const val TAG = "ServiceRestartWorker"

        fun startServiceViaWorker(context: Context) {
            Log.d(TAG, "startServiceViaWorker called")
            // As per Documentation: The minimum repeat interval that can be defined is 15 minutes
            // (same as the JobScheduler API), but in practice 15 doesn't work. Using 16 here
            val request: PeriodicWorkRequest =
                PeriodicWorkRequest
                    .Builder(ServiceRestartWorker::class.java, 16, TimeUnit.MINUTES)
                    .build()

            // to schedule a unique work, no matter how many times app is opened i.e. startServiceViaWorker gets called
            // do check for AutoStart permission
            WorkManager.getInstance(context)
                .enqueueUniquePeriodicWork("StartServiceWorker", ExistingPeriodicWorkPolicy.KEEP, request)
        }
    }

    override fun doWork(): Result {
        Log.d(TAG, "doWork called for: " + this.id)
        Log.d(TAG, "Service Running: " + NetworkObserverService.isServiceRunning)
        NetworkObserverService.startService(this.context)
        return Result.success()
    }

    override fun onStopped() {
        Log.d(TAG, "onStopped called for: " + this.id)
        super.onStopped()
    }
}