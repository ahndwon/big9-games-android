package kr.co.big9.games.ui

import android.Manifest
import android.bluetooth.BluetoothDevice
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.support.v4.content.ContextCompat
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.view.Window
import kotlinx.android.synthetic.main.fragment_bluetooth_dialog.view.*
import kr.co.big9.games.R
import kr.co.big9.games.bluetooth.BLEManager
import kr.co.big9.games.listener.BLEManagerListener
import kr.co.big9.games.ui.adapter.DeviceListAdapter
import org.jetbrains.anko.runOnUiThread
import java.util.*


class BluetoothDialogFragment : DialogFragment() {

    companion object {
        val TAG: String = BluetoothDialogFragment::class.java.name
        const val INTERVAL = 50.toLong()
        private const val PERMISSION_ACCESS_FINE_LOCATION = 100
    }

    private val bleManager = BLEManager()
    var deviceAddress: String? = null
    var deviceList = HashMap<String, BluetoothDevice>()

    var onClickListener : View.OnClickListener? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val view =
                inflater.inflate(R.layout.fragment_bluetooth_dialog, container, false)

        if (dialog != null && dialog.window != null) {
            dialog.window.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            dialog.window.requestFeature(Window.FEATURE_NO_TITLE)
        }

        view.findProgressBar.visibility = View.VISIBLE
        checkBluetoothPermission()

        val adapter = DeviceListAdapter()
        adapter.deviceList = deviceList
        adapter.onClick = { v ->
            onClickListener?.onClick(v)
        }
        view.deviceRecyclerView.adapter = adapter
        view.deviceRecyclerView.layoutManager = LinearLayoutManager(requireContext())

        bleManager.listener = object : BLEManagerListener {
            override fun onFindDevice(device: BluetoothDevice) {
                if (this@BluetoothDialogFragment.isAdded) {
                    requireContext().runOnUiThread {
                        view.findProgressBar.visibility = View.GONE
                        if (!deviceList.contains(device.address)) {
                            deviceList[device.address] = device
                            view.deviceRecyclerView.adapter?.notifyDataSetChanged()
                        }
                    }
                }
            }

            override fun onFailedToFindDevice() {
                if (this@BluetoothDialogFragment.isAdded) {
                    requireContext().runOnUiThread {
                        view.noDeviceText.visibility = View.VISIBLE
                        view.findProgressBar.visibility = View.GONE
                    }
                }
            }

            override fun onConnected() { }

            override fun onServiceReady() { }

            override fun onDisconnected() { }

            override fun onData(data: ByteArray) { }
        }

        view.deviceSearchButton.setOnClickListener {
            view.noDeviceText.visibility = View.GONE
            view.findProgressBar.visibility = View.VISIBLE
            bleManager.scan()
        }

        return view
    }

    private fun checkBluetoothPermission() {
        if (ContextCompat.checkSelfPermission(requireContext(),
                        Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            bleManager.scan()
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                        PERMISSION_ACCESS_FINE_LOCATION)
            }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        if (requestCode == PERMISSION_ACCESS_FINE_LOCATION) {
            if (permissions.size == 1 && permissions[0] == Manifest.permission.ACCESS_FINE_LOCATION) {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    bleManager.scan()
                } else {
//                    toast("permission required: ACCESS_FINE_LOCATION")
//                    finish()
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if (showsDialog) {
            val metrics = activity?.resources?.displayMetrics
            metrics?.let {
                //                val windowManager = requireContext().getSystemService(WINDOW_SERVICE) as WindowManager
//                windowManager.defaultDisplay.getMetrics(metrics)
//                val widthInDP = Math.round(metrics.widthPixels / metrics.density)

                val dialogWidth = Math.min(metrics.widthPixels - 230, metrics.heightPixels)
                dialog.window?.setLayout(dialogWidth, WRAP_CONTENT)
            }

            metrics?.let {

            }
        }
    }
}