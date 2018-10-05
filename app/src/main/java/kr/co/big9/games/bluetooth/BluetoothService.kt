package kr.co.big9.games.bluetooth

import android.app.Service
import android.bluetooth.*
import android.content.Context
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import android.support.v4.content.LocalBroadcastManager
import android.util.Log
import java.util.*

class BluetoothService : Service() {
    inner class LocalBinder : Binder() {
        val service: BluetoothService
            get() = this@BluetoothService
    }

    private val mBinder: IBinder = LocalBinder()
    private var mBluetoothGatt: BluetoothGatt? = null
    private var mBluetoothManager: BluetoothManager? = null
    private var mBluetoothAdapter: BluetoothAdapter? = null
    private var mBluetoothDeviceAddress: String? = null

    private var mConnectionState: Int = STATE_DISCONNECTED

    override fun onBind(intent: Intent?): IBinder {
        return mBinder
    }

    override fun onUnbind(intent: Intent?): Boolean {
        close()
        return super.onUnbind(intent)
    }

    fun initialize() {
        mBluetoothManager = getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        mBluetoothAdapter = mBluetoothManager?.adapter
    }

    fun connect(address: String): Boolean {
        val device: BluetoothDevice? = mBluetoothAdapter?.getRemoteDevice(address)
        if (device == null) {
            Log.w(TAG, "Device not found. Unable to connect")
            return false
        }

        mBluetoothGatt = device.connectGatt(this, false, mGattCallback)
        Log.d(TAG, "Trying to create a new connection")

        mBluetoothDeviceAddress = address
        mConnectionState = STATE_CONNECTING
        return true
    }

    fun disconnect() {
        mBluetoothGatt?.disconnect()
    }

    fun close() {
        mBluetoothGatt?.close()
    }

    fun readCharacteristic(characteristic: BluetoothGattCharacteristic) {
        mBluetoothGatt?.readCharacteristic(characteristic)
    }

    fun enableTXNotification() {
        val rxService = mBluetoothGatt?.getService(RX_SERVICE_UUID)
        if (rxService == null) {
            Log.d(TAG, "Rx service not found")
            broadcastUpdate(DEVICE_DOES_NOT_SUPPORT_UART)
            return
        }

        val txChar = rxService.getCharacteristic(TX_CHAR_UUID)
        if (txChar == null) {
            Log.d(TAG, "Tx characteristic not found")
            broadcastUpdate(DEVICE_DOES_NOT_SUPPORT_UART)
            return
        }

        mBluetoothGatt?.setCharacteristicNotification(txChar, true)

        val descriptor = txChar.getDescriptor(CCCD)
        descriptor.value = BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
        mBluetoothGatt?.writeDescriptor(descriptor)
    }

    fun writeRxCharacteristic(value: ByteArray) {
        val rxService = mBluetoothGatt?.getService(RX_SERVICE_UUID)
        if (rxService == null) {
            Log.d(TAG, "Rx service not found")
            broadcastUpdate(DEVICE_DOES_NOT_SUPPORT_UART)
            return
        }

        val rxChar = rxService.getCharacteristic(RX_CHAR_UUID)
        if (rxChar == null) {
            Log.d(TAG, "Rx characteristic not found")
            broadcastUpdate(DEVICE_DOES_NOT_SUPPORT_UART)
            return
        }

        rxChar.value = value
        val status = mBluetoothGatt?.writeCharacteristic(rxChar)

        // Log.d(TAG, "writeRxCharacteristic: status=$status")
    }

    fun getSupportedGattServices(): List<BluetoothGattService> {
        mBluetoothGatt?.let {
            return it.services
        }

        return emptyList()
    }

    private val mGattCallback = object : BluetoothGattCallback() {
        override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
            when (newState) {
                STATE_CONNECTED -> {
                    mConnectionState = STATE_CONNECTED
                    broadcastUpdate(ACTION_GATT_CONNECTED)
                    Log.i(TAG, "Attempting to start service discovery:" +
                            mBluetoothGatt?.discoverServices())
                }
                STATE_DISCONNECTED -> {
                    mConnectionState = STATE_DISCONNECTED
                    broadcastUpdate(ACTION_GATT_DISCONNECTED)
                }
            }
        }

        override fun onServicesDiscovered(gatt: BluetoothGatt,
                                          status: Int) {
            if (status != BluetoothGatt.GATT_SUCCESS)
                return

            broadcastUpdate(ACTION_GATT_SERVICES_DISCOVERED)
        }

        override fun onCharacteristicRead(gatt: BluetoothGatt?,
                                          characteristic: BluetoothGattCharacteristic,
                                          status: Int) {
            if (status != BluetoothGatt.GATT_SUCCESS)
                return

            broadcastUpdate(ACTION_DATA_AVAILABLE, characteristic)
        }

        override fun onCharacteristicChanged(gatt: BluetoothGatt?,
                                             characteristic: BluetoothGattCharacteristic) {
            broadcastUpdate(ACTION_DATA_AVAILABLE, characteristic)
        }
    }

    private fun broadcastUpdate(action: String) {
        LocalBroadcastManager.getInstance(this).sendBroadcast(Intent(action))
    }

    private fun broadcastUpdate(action: String, characteristic: BluetoothGattCharacteristic) {
        val intent = Intent(action)
        if (characteristic.uuid == TX_CHAR_UUID)
            intent.putExtra(EXTRA_DATA, characteristic.value)

        LocalBroadcastManager.getInstance(this).sendBroadcast(intent)
    }

    companion object {
        val TAG: String = BluetoothService::class.java.simpleName

        const val STATE_DISCONNECTED = 0
        const val STATE_CONNECTING = 1
        const val STATE_CONNECTED = 2

        const val ACTION_GATT_CONNECTED = "com.nordicsemi.nrfUART.ACTION_GATT_CONNECTED"
        const val ACTION_GATT_DISCONNECTED = "com.nordicsemi.nrfUART.ACTION_GATT_DISCONNECTED"
        const val ACTION_GATT_SERVICES_DISCOVERED = "com.nordicsemi.nrfUART.ACTION_GATT_SERVICES_DISCOVERED"
        const val ACTION_DATA_AVAILABLE = "com.nordicsemi.nrfUART.ACTION_DATA_AVAILABLE"
        const val EXTRA_DATA = "com.nordicsemi.nrfUART.EXTRA_DATA"
        const val DEVICE_DOES_NOT_SUPPORT_UART = "com.nordicsemi.nrfUART.DEVICE_DOES_NOT_SUPPORT_UART"

        val TX_POWER_UUID = UUID.fromString("00001804-0000-1000-8000-00805f9b34fb")!!
        val TX_POWER_LEVEL_UUID = UUID.fromString("00002a07-0000-1000-8000-00805f9b34fb")!!
        val CCCD = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb")!!
        val FIRMWARE_REVISON_UUID = UUID.fromString("00002a26-0000-1000-8000-00805f9b34fb")!!
        val DIS_UUID = UUID.fromString("0000180a-0000-1000-8000-00805f9b34fb")!!
        val RX_SERVICE_UUID = UUID.fromString("6e400001-b5a3-f393-e0a9-e50e24dcca9e")!!
        val RX_CHAR_UUID = UUID.fromString("6e400002-b5a3-f393-e0a9-e50e24dcca9e")!!
        val TX_CHAR_UUID = UUID.fromString("6e400003-b5a3-f393-e0a9-e50e24dcca9e")!!
    }

}