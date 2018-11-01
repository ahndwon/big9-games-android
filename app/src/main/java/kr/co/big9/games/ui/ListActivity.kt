package kr.co.big9.games.ui

import android.app.Activity
import android.app.ProgressDialog
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.Intent
import android.os.Bundle
import android.preference.PreferenceManager
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import kotlinx.android.synthetic.main.activity_list.*
import kotlinx.android.synthetic.main.fragment_bluetooth_dialog.view.*
import kotlinx.android.synthetic.main.item_device.view.*
import kr.co.big9.games.R
import kr.co.big9.games.bluetooth.BLEManager
import kr.co.big9.games.listener.BLEManagerListener
import kr.co.big9.games.model.Stage
import kr.co.big9.games.ui.adapter.StageListAdapter
import kr.co.big9.games.utils.*
import org.jetbrains.anko.design.snackbar
import org.jetbrains.anko.indeterminateProgressDialog
import org.jetbrains.anko.startActivity

class ListActivity : AppCompatActivity() {
    companion object {
        const val REQUEST_ENABLE_BT = 333
        val TAG: String = ListActivity::class.java.name
    }

    private val bleManager = BLEManager()
    var isShowing = false
    lateinit var bluetoothDialog: ProgressDialog


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_list)
        setSupportActionBar(toolbar)
        supportActionBar?.let {
            title = ""
        }

        val list = ArrayList<Stage>().apply {
            this.add(Stage("똥피하기", 1, POO_SCENE,10000))
            this.add(Stage("길건너기", 2, CRT_SCENE,10000))
//            for (i in 0..1)
//                this.add(Stage("$i", i, 10000))
        }


        val stageListAdapter = StageListAdapter(list)
//        stageListAdapter.onClick = { v ->
//            Log.d(TAG, "onClick")
//            startActivity<UnityPlayerActivity>()
//        }
        stageRecyclerView.adapter = stageListAdapter
        stageRecyclerView.layoutManager = LinearLayoutManager(this)

        setBluetoothProgressDialog()

        if (checkBluetooth(this)) bleManager.scan()


        bleManager.listener = object : BLEManagerListener {
            override fun onFindDevice(device: BluetoothDevice) {
                runOnUiThread {
                    dismissDialog()
                }
            }

            override fun onFailedToFindDevice() {
                Log.d(TAG, "onFailedToFindDevice")
                runOnUiThread {
                    if (isShowing) {
                        bluetoothDialog.dismiss()
                        isShowing = false
                        showBluetoothDialogFragment()
                    }
                }
            }

            override fun onConnected() {}

            override fun onServiceReady() {}

            override fun onDisconnected() {
                Log.d(TAG, "onDisconnected")
            }

            override fun onData(data: ByteArray) {}
        }

    }

    private fun dismissDialog() {
        if (isShowing) {
            bluetoothDialog.dismiss()
            isShowing = false
        }
    }

    private fun setBluetoothProgressDialog() {
        bluetoothDialog = indeterminateProgressDialog("장치 검색중...")
//        bluetoothDialog.setCanceledOnTouchOutside(false)
        bluetoothDialog.show()
        isShowing = true
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.home_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId) {
            R.id.action_bluetooth -> {
                if (checkBluetooth(this)) showBluetoothDialogFragment()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun checkBluetooth(activity: Activity): Boolean {
        BluetoothAdapter.getDefaultAdapter()?.let { adapter ->
            return if (!adapter.isEnabled) {
                val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
                activity.startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT)
                false
            } else true
        }
        return false
    }

    private fun showBluetoothDialogFragment() {
        BluetoothDialogFragment().apply {
            onClickListener = View.OnClickListener { button ->
                val device =
                        this.view?.deviceRecyclerView?.getChildViewHolder(button)
                val address = device?.itemView?.deviceAddress?.text.toString()
                PreferenceManager.getDefaultSharedPreferences(this.context)
                        .edit()
                        .putString("DEVICE_ADDRESS", address)
                        .apply()
                Log.d(BluetoothDialogFragment.TAG, "clicked address : "
                        + device?.itemView?.deviceAddress?.text.toString())

                val saved = PreferenceManager.getDefaultSharedPreferences(this.context)
                        .getString("DEVICE_ADDRESS", null)
                Log.d(TAG, "Address : $address")
                Log.d(TAG, "saved : $saved")
                if (saved == address) {
                    dismiss()
                    snackbar(this@ListActivity.listActivityLayout,
                            getString(R.string.text_bluetooth_found_ko))
                }
            }

        }.show(supportFragmentManager, "BluetoothDialog")
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        Log.d(TAG, "requestCode : $requestCode")
        if (requestCode == PLAY_MODE_REQUEST_CODE) {
            if (resultCode == BT_FAILURE) {
                this.showBluetoothDialogFragment()
                snackbar(listActivityLayout, "블루투스 연결을 확인해주세요")
            }

            if (resultCode == RESULT_OK) {
                Log.d(TAG, "RESULT_OK")
            }
        }

        if (requestCode == REQUEST_ENABLE_BT) {
            if (resultCode == RESULT_OK) {
                Log.d(TAG, "REQUEST_ENABLE_BT")

                bleManager.scan()
            }
        }

//        if (requestCode == NO_PERMISSION) {
//            snackbar(exerciseSelectLayout, "권한에 동의가 필요합니다.")
//        }
    }

}
