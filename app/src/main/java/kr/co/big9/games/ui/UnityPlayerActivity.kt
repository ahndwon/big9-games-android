package kr.co.big9.games.ui

import android.Manifest
import android.bluetooth.BluetoothDevice
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.graphics.PixelFormat
import android.os.Build
import android.os.Bundle
import android.preference.PreferenceManager
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.KeyEvent
import android.view.MotionEvent
import android.view.WindowManager
import android.widget.FrameLayout
import com.unity3d.player.UnityPlayer
import kr.co.big9.games.R
import kr.co.big9.games.bluetooth.BLEManager
import kr.co.big9.games.listener.BLEManagerListener
import kr.co.big9.games.listener.OnGameFinishListener
import kr.co.big9.games.listener.OnInitializeListener
import kr.co.big9.games.listener.OnMotionListener
import kr.co.big9.games.model.GameManager
import kr.co.big9.games.utils.BT_FAILURE
import kr.co.big9.games.utils.GameCountHelper
import kr.co.big9.games.utils.GameCounter
import java.util.*


class UnityPlayerActivity : AppCompatActivity() {

    companion object {
        private val TAG = UnityPlayerActivity::class.java.name

        // Constants
        private const val PERMISSION_ACCESS_FINE_LOCATION = 100
        private const val INTERVAL = 50
        private var exerciseTime: Long = 0
        var animationCount: Int = 0

        lateinit var gameManager: GameManager // 반드시 static 이여야 함
    }

    // don't change the name of this variable; referenced from native code
    private lateinit var mUnityPlayer: UnityPlayer

    // members
    private var stage: Int = 0
    private var time: Int = 0

    lateinit var gameCountHelper: GameCountHelper

    private var buf = StringBuilder()
    private val bleManager = BLEManager()

    private var deviceAddress: String? = null
    private var receiveDataTimer: Timer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_unity)

        val frameLayout = findViewById<FrameLayout>(R.id.frame_layout_for_unity_player)

        bleManager.onInitializeListener = object : OnInitializeListener {
            override fun onInitialize() {
                connectDevice()
            }
        }
        //        requestWindowFeature(Window.FEATURE_NO_TITLE);
        checkBluetoothPermission()


        initUnitySettings()
        frameLayout.addView(mUnityPlayer.view)

        mUnityPlayer.requestFocus()


        bleManager.listener = object : BLEManagerListener {
            override fun onFindDevice(device: BluetoothDevice) {}

            override fun onFailedToFindDevice() {}

            override fun onConnected() {
                Log.i(TAG, "bluetooth connected")
            }

            override fun onServiceReady() {
                Log.i(TAG, "onServiceReady")
                startActionSensor()
            }

            override fun onDisconnected() {
                UnityPlayer.UnitySendMessage("Player", "CloseApp", "")
                setResult(BT_FAILURE)
                finish()
            }

            override fun onData(data: ByteArray) {
                for (d in data) {
                    if (d == '#'.toByte()) {
                        gameManager.countExercise()
                        gameCountHelper.processData(buf.toString())
                        UnityPlayer.UnitySendMessage("BLEBridge", "OnReceiveData", buf.toString())
                        buf = StringBuilder()
                    } else {
                        buf.append(d.toChar())
                    }
                }
            }
        }



    }

    private fun connectDevice() {
        deviceAddress = PreferenceManager.getDefaultSharedPreferences(this)
                .getString("DEVICE_ADDRESS", null)
        Log.d(TAG, "connecting address:$deviceAddress")

        if (deviceAddress == null) {
            setResult(BT_FAILURE)
            UnityPlayer.UnitySendMessage("Player", "CloseApp", "")
            finish()
        }

        deviceAddress?.let {
            bleManager.connect(it)
        }
    }

    private fun startActionSensor() {
        if (receiveDataTimer != null) {
            receiveDataTimer?.cancel()
            receiveDataTimer = null
        }

        receiveDataTimer = Timer()
        receiveDataTimer?.scheduleAtFixedRate(object : TimerTask() {
            override fun run() {
                bleManager.writeToDevice("A".toByteArray())
            }

        }, 0, INTERVAL.toLong())
    }

    private fun initUnitySettings() {
        mUnityPlayer = UnityPlayer(this)

        window.setFormat(PixelFormat.RGBX_8888)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE

        gameCountHelper = GameCountHelper()
        gameManager = GameManager().apply {
            this.gameCounter = GameCounter().apply {
                this.countHelper = gameCountHelper
                this.onMotionListener = (object : OnMotionListener {
                    override fun onRight() {
                        Log.d(GameManager.TAG, "RIGHT")
                        UnityPlayer.UnitySendMessage("BLEBridge", "OnReceiveMotion", "RIGHT")
                    }

                    override fun onLeft() {
                        Log.d(GameManager.TAG, "LEFT")
                        UnityPlayer.UnitySendMessage("BLEBridge", "OnReceiveMotion", "LEFT")
                    }
                })}

            this.finishListener = object : OnGameFinishListener {
                override fun finishExercise(star: Int, score: Int, maxCount: Int) {
                    endData()
                    finish()
                }
            }
        }


    }

    private fun checkBluetoothPermission() {
        if (ContextCompat.checkSelfPermission(this,
                        Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            initBluetooth()
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                val permission = arrayOf(Manifest.permission.ACCESS_FINE_LOCATION)
                requestPermissions(permission,
                        PERMISSION_ACCESS_FINE_LOCATION)
            }
        }
    }

    private fun initBluetooth() {
        if (!bleManager.initialize(this)) {
            Log.e("BLUETOOTH", "Bluetooth manager initialize failed")
            finish()
        }
    }

    private fun endData() {
        if (receiveDataTimer != null) {
            receiveDataTimer?.cancel()
            receiveDataTimer = null
        }
    }


    override fun onNewIntent(intent: Intent) {
        // To support deep linking, we need to make sure that the client can get access to
        // the last sent intent. The clients access this through a JNI api that allows them
        // to get the intent set on launch. To update that after launch we have to manually
        // replace the intent with the one caught here.
        setIntent(intent)
    }

    // Quit Unity
    override fun onDestroy() {
        Log.d(TAG, "onDestroy")
        endData()
//        bleManager.disconnect()
//        bleManager.destroy(this)
//        mUnityPlayer.quit()
        super.onDestroy()
    }

    // Pause Unity
    override fun onPause() {
        super.onPause()
        mUnityPlayer.pause()
    }

    // Resume Unity
    override fun onResume() {
        super.onResume()
        mUnityPlayer.resume()
    }

    override fun onStart() {
        super.onStart()
        mUnityPlayer.start()
    }

    override fun onStop() {
        super.onStop()
        mUnityPlayer.stop()
    }

    // Low Memory Unity
    override fun onLowMemory() {
        super.onLowMemory()
        mUnityPlayer.lowMemory()
    }

    // Trim Memory Unity
    override fun onTrimMemory(level: Int) {
        super.onTrimMemory(level)
        if (level == TRIM_MEMORY_RUNNING_CRITICAL) {
            mUnityPlayer.lowMemory()
        }
    }

    // This ensures the layout will be correct.
    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        mUnityPlayer.configurationChanged(newConfig)
    }

    // Notify Unity of the focus change.
    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        mUnityPlayer.windowFocusChanged(hasFocus)
    }

    // For some reason the multiple keyevent type is not supported by the ndk.
    // Force event injection by overriding dispatchKeyEvent().
    override fun dispatchKeyEvent(event: KeyEvent): Boolean {
        return if (event.action == KeyEvent.ACTION_MULTIPLE) mUnityPlayer.injectEvent(event)
        else super.dispatchKeyEvent(event)
    }

    // Pass any events not handled by (unfocused) views straight to UnityPlayer
    override fun onKeyUp(keyCode: Int, event: KeyEvent): Boolean {
        return mUnityPlayer.injectEvent(event)
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            Log.d(TAG, "KEYCODE_BACK")
            UnityPlayer.UnitySendMessage("Player", "CloseApp", "")
            onBackPressed()
        }
        return mUnityPlayer.injectEvent(event)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        return mUnityPlayer.injectEvent(event)
    }

    /*API12*/
    override fun onGenericMotionEvent(event: MotionEvent): Boolean {
        return mUnityPlayer.injectEvent(event)
    }

    fun checkAnimationEnd() {
        Log.d(TAG, "checkAnimationEnd()")
    }

    fun closeApp() {
        Log.d(TAG, "closeApp()")
    }

    fun countDownFinish() {
        Log.d(TAG, "countDownFinish()")
        println("카운트다운 끝")
        exerciseTime = System.currentTimeMillis()
    }

    fun endUnity() {
        Log.d(TAG, "endUnity()")
    }

    fun printLog(message: String) {
        Log.d(TAG, "printLog()")

    }

    fun showDialog(message: String) {
        Log.d(TAG, "showDialog()")
    }

}