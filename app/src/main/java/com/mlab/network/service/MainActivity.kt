package com.mlab.network.service

import android.os.Bundle
import androidx.activity.ComponentActivity
import com.mlab.network.service.service.ServiceRestartWorker


class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        ServiceRestartWorker.startServiceViaWorker(this)
    }
}

