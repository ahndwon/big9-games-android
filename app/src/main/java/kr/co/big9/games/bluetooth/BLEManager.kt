package kr.co.big9.games.bluetooth

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.le.BluetoothLeScanner
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.content.*
import android.os.IBinder
import android.support.v4.content.LocalBroadcastManager
import android.util.Log
import kr.co.big9.games.listener.BLEManagerListener
import kr.co.big9.games.listener.OnInitializeListener
import java.util.*
import kotlin.concurrent.timerTask


class BLEManager {
    companion object {
        const val UART_PROFILE_READY = 10
        const val UART_PROFILE_CONNECTED = 20
        const val UART_PROFILE_DISCONNECTED = 21
    }

    val TAG: String = BLEManager::class.java.name

    lateinit var bluetoothService: BluetoothService
    lateinit var leScanner: BluetoothLeScanner
    var bluetoothAdapter: BluetoothAdapter? = null

    var isInitialized = false
    var isBinded = false
        private set
    var isScanning = false
        private set
    var isConnected = false

    var listener: BLEManagerListener? = null
    var timer: Timer? = null


    var device: BluetoothDevice? = null
    var deviceAddress: String? = null

    var mState = UART_PROFILE_DISCONNECTED
    private val mIntentFilter: IntentFilter by lazy {
        val filter = IntentFilter()
        filter.addAction(BluetoothService.ACTION_GATT_CONNECTED)
        filter.addAction(BluetoothService.ACTION_GATT_DISCONNECTED)
        filter.addAction(BluetoothService.ACTION_GATT_SERVICES_DISCOVERED)
        filter.addAction(BluetoothService.ACTION_DATA_AVAILABLE)
        filter.addAction(BluetoothService.DEVICE_DOES_NOT_SUPPORT_UART)
        filter
    }

    var onInitializeListener: OnInitializeListener? = null

    private val mServiceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            Log.d(TAG, "onServiceConnected" + service)
            service as BluetoothService.LocalBinder

            bluetoothService = service.service
            bluetoothService.initialize()

            onInitializeListener?.onInitialize()

//            BLEManager.service = service.service
//            BLEManager.service.initialize()
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            Log.i(TAG, "disconnected")
        }
    }

    private val mUARTStatusChangeReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            when (intent!!.action) {
                BluetoothService.ACTION_GATT_CONNECTED -> {
                    timer?.cancel()

                    mState = UART_PROFILE_CONNECTED
                    Log.i(TAG, "UART_PROFILE_CONNECTED")

                    listener?.onConnected()
                    isConnected = true
                }

                BluetoothService.ACTION_GATT_DISCONNECTED -> {
                    bluetoothService.close()
                    mState = UART_PROFILE_DISCONNECTED
                    Log.i(TAG, "ACTION_GATT_DISCONNECTED")

                    listener?.onDisconnected()
                    isConnected = false
                }

                BluetoothService.ACTION_GATT_SERVICES_DISCOVERED -> {
                    Log.i(TAG, "ACTION_GATT_SERVICES_DISCOVERED")
                    bluetoothService.enableTXNotification()
                    listener?.onServiceReady()
                }

                BluetoothService.ACTION_DATA_AVAILABLE -> {
                    val txValue = intent.getByteArrayExtra(BluetoothService.EXTRA_DATA)
                    listener?.onData(txValue)
                }

                BluetoothService.DEVICE_DOES_NOT_SUPPORT_UART -> {
                    bluetoothService.disconnect()
                }
            }
        }
    }

    private fun initializeService(context: Context) {
        val bindIntent = Intent(context, BluetoothService::class.java)
        Log.d(TAG, "Service binded context : $context")
        if (!isBinded) {
            context.bindService(bindIntent, mServiceConnection, Context.BIND_AUTO_CREATE)
            isBinded = true
        }
//        isBinded = context.bindService(bindIntent, mServiceConnection, Context.BIND_AUTO_CREATE)

        LocalBroadcastManager.getInstance(context)
                .registerReceiver(mUARTStatusChangeReceiver, mIntentFilter)
    }

    private fun initializeBluetooth(): Boolean {
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
        if (bluetoothAdapter == null) {
            return false
        }

        bluetoothAdapter?.let {
            leScanner = it.bluetoothLeScanner
        }

        return true
    }

    fun initialize(context: Context): Boolean {
        Log.d(TAG, "initialize context = ${context}")
        if (isInitialized) {
            Log.d(TAG, "destroy on isInitialized")

            destroy(context)
        }

        initializeService(context)
        isInitialized = initializeBluetooth()

        return isInitialized
    }

    fun destroy(context: Context) {
        Log.d(TAG, "destroy on ${context.javaClass}")
        Log.d(TAG, "destroy context = ${context}")
        LocalBroadcastManager.getInstance(context).unregisterReceiver(mUARTStatusChangeReceiver)
        if (isBinded) {
            Log.d(TAG, "unbindService")
            Log.d(TAG, "Service un_binded context : $context")

            context.unbindService(mServiceConnection)
            isBinded = false
            bluetoothService.stopSelf()
            bluetoothService.close()
            isInitialized = false
        }
    }

    val mScanCallback = object : ScanCallback() {
        override fun onScanFailed(errorCode: Int) {
            Log.i(TAG, "onScanFailed")
        }

        override fun onScanResult(callbackType: Int, result: ScanResult?) {
            Log.d("BLEManager", "result: ${result.toString()}")
            Log.d("BLEManager", "name: ${result?.device?.name.toString()}")
            val deviceName = result?.device?.name
            deviceName?.let {
                if (deviceName.contains("Big9", true)) {
                    Log.i(TAG, "onScanResult - Big9")

//                    Log.i(TAG, device?.address)
//                    Log.i(TAG, device? .address)
//                    Logdevice?.type

                    timer?.cancel()
                    listener?.onFindDevice(result.device)
                    leScanner.stopScan(this)
//                    service.connect(result.device.address)
                }
            }

            result?.device?.let {
                Log.i("device", result.device?.toString())
                Log.i("bondState", result.device?.bondState.toString())
                Log.i("type", result.device?.type.toString())
                Log.i("name", result.device?.name.toString())
            }
        }
    }


    fun scan() {
        Log.i(TAG, "scan()")
//        check(isInitialized)
        disconnect()

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()

        bluetoothAdapter?.bluetoothLeScanner?.let {
            leScanner = it
            leScanner.startScan(mScanCallback)
            isScanning = true

            timer = Timer()
            timer?.schedule(timerTask {
                stopScan()
                listener?.onFailedToFindDevice()
            }, BleConfig.SCAN_TIMEOUT)
        }
    }

    fun connect(address: String) {
        bluetoothService.connect(address)
        deviceAddress = address
//        isConnected = true
    }

    fun disconnect() {
        Log.i(TAG, "disconnect")

        if (mState == UART_PROFILE_CONNECTED) {
//            isConnected = false
            bluetoothService.disconnect()
            bluetoothService.close()
        }
    }

    fun stopScan() {
        Log.i("stopScan", "disconnect")
        if (!isScanning)
            return


        leScanner.stopScan(mScanCallback)

        isScanning = false
    }

    fun writeToDevice(data: ByteArray) {
        if (mState != UART_PROFILE_CONNECTED)
            return

        bluetoothService.writeRxCharacteristic(data)
    }
}