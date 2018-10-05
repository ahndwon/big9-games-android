package kr.co.big9.games.listener

import android.bluetooth.BluetoothDevice

interface BLEManagerListener {
    fun onFindDevice(device: BluetoothDevice)
    fun onFailedToFindDevice()
    fun onConnected()
    fun onServiceReady()
    fun onDisconnected()
    fun onData(data: ByteArray)
}