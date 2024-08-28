package com.mlab.network.service.service

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.os.Build
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class NetworkObserver(context: Context) {
    private val connectivityManager =
        context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    private val _networkStatus = MutableStateFlow(Status.Idle)
    val networkStatus = _networkStatus.asStateFlow()

    var isNetworkAvailable = isConnected(context)
        private set

    enum class Status(val message: String) {
        Available("Hurray! Network is back"),
        Unavailable("No Network Available"),
        Losing("Poor Network Connection"),
        Lost("Lost Network Connection"),
        Idle("Idle")
    }

    init {
        _networkStatus.value = if (isNetworkAvailable) Status.Available else Status.Unavailable

        val callback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                super.onAvailable(network)
                _networkStatus.value = Status.Available
                isNetworkAvailable = true
            }

            override fun onLosing(network: Network, maxMsToLive: Int) {
                super.onLosing(network, maxMsToLive)
                _networkStatus.value = Status.Losing
            }

            override fun onLost(network: Network) {
                super.onLost(network)
                _networkStatus.value = Status.Lost
                isNetworkAvailable = false
            }

            override fun onUnavailable() {
                super.onUnavailable()
                _networkStatus.value = Status.Unavailable
                isNetworkAvailable = false
            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
            connectivityManager.registerDefaultNetworkCallback(callback)
        else
            connectivityManager.registerNetworkCallback(NetworkRequest.Builder().build(), callback)

    }

    private fun isConnected(context: Context): Boolean {
        var status = false
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            cm.run {
                getNetworkCapabilities(activeNetwork)?.run {
                    status = hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
                            hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) ||
                            hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) ||
                            hasTransport(NetworkCapabilities.TRANSPORT_BLUETOOTH) ||
                            hasTransport(NetworkCapabilities.TRANSPORT_VPN)
                }
            }
        } else {
            @Suppress("DEPRECATION")
            cm.activeNetworkInfo?.isConnected?.let { status = it}
        }
        return status
    }
}
