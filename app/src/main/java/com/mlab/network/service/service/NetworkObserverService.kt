package com.mlab.network.service.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale
import kotlin.time.Duration.Companion.minutes


class NetworkObserverService : Service() {

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "onCreate called")
        createNotificationChannel()
        isServiceRunning = true
    }

    override fun onBind(intent: Intent) = null

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        Log.d(TAG, "onStartCommand called")
        val notification: Notification = NotificationCompat.Builder(this, "NOTIFICATION_CHANNEL")
            .setContentTitle("Network Observer Service is Running")
            .setContentText("Listening for network states")
            .setSmallIcon(android.R.drawable.ic_secure)
            .build()
        startForeground(1, notification)

        var networkCallJob: Job? = null
        CoroutineScope(Dispatchers.Main.immediate).launch {
            NetworkObserver(this@NetworkObserverService).networkStatus.collect {
                Log.d(TAG, "networkStatus= $it, lastNetworkAvailableTime= $lastNetworkAvailableTime")
                when (it) {
                    NetworkObserver.Status.Available, NetworkObserver.Status.Losing -> {
                        networkCallJob?.cancel()
                        networkCallJob = CoroutineScope(Dispatchers.Main.immediate).launch {
                            while (true) {
                                lastNetworkAvailableTime =
                                    SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                                        .format(System.currentTimeMillis())
                                Log.d(TAG, "networkStatus= $it, lastNetworkAvailableTime= $lastNetworkAvailableTime")


                                /**   do network call here and save the 'lastNetworkAvailableTime'  */


                                delay(15.minutes)
                            }

                        }
                    }

                    NetworkObserver.Status.Unavailable, NetworkObserver.Status.Lost -> networkCallJob?.cancel()
                    else -> Unit
                }
            }
        }
        return START_STICKY
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            getSystemService(NotificationManager::class.java)
                .createNotificationChannel(
                    NotificationChannel("NOTIFICATION_CHANNEL", TAG, NotificationManager.IMPORTANCE_DEFAULT)
                )
        }
    }

    override fun onDestroy() {
        Log.d(TAG, "onDestroy called")
        isServiceRunning = false
        stopSelf()
        // call WorkRestartReceiver which will restart this service via a worker
        sendBroadcast(Intent(this, WorkRestartReceiver::class.java))

        super.onDestroy()
    }

    companion object {
        var isServiceRunning: Boolean = false
        var lastNetworkAvailableTime: String = ""
        const val TAG = "NetworkObserverService"

        fun startService(context: Context) {
            if (!isServiceRunning) {
                Log.d(TAG, "Starting Network Observer Service")
                val intent = Intent(context, NetworkObserverService::class.java)
                ContextCompat.startForegroundService(context, intent)
            }
        }

    }
}