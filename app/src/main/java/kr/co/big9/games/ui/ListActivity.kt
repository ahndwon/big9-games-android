package kr.co.big9.games.ui

import android.app.Activity
import android.bluetooth.BluetoothAdapter
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
import kr.co.big9.games.model.Stage
import kr.co.big9.games.ui.adapter.StageListAdapter
import org.jetbrains.anko.design.snackbar
import org.jetbrains.anko.startActivity

class ListActivity : AppCompatActivity() {
    companion object {
        const val REQUEST_ENABLE_BT = 333
        val TAG: String = ListActivity::class.java.name
    }

    private val bleManager = BLEManager()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_list)
        setSupportActionBar(toolbar)
        supportActionBar?.let {
            title=""
        }

        val list = ArrayList<Stage>().apply {
            for (i in 0..5)
            this.add(Stage("$i", i, 10000))
        }


        val stageListAdapter = StageListAdapter(list)
        stageListAdapter.onClick = { v ->
            Log.d(TAG, "onClick")
            startActivity<UnityPlayerActivity>()
        }
        stageRecyclerView.adapter = stageListAdapter
        stageRecyclerView.layoutManager = LinearLayoutManager(this)

    }


    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.home_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId) {
            R.id.action_bluetooth -> {
                checkBluetooth(this)

//                requireContext().startActivity<BluetoothActivity>()
                BluetoothDialogFragment().apply {
                    onClickListener = View.OnClickListener { v ->
                        val device =
                                this.view?.deviceRecyclerView?.getChildViewHolder(v)
                        val address = device?.itemView?.deviceAddress?.text.toString()
                        PreferenceManager.getDefaultSharedPreferences(this.context)
                                .edit()
                                .putString("DEVICE_ADDRESS", address)
                                .apply()
                        Log.d(BluetoothDialogFragment.TAG, "clicked address : "
                                + device?.itemView?.deviceAddress?.text.toString())

                        val saved = PreferenceManager.getDefaultSharedPreferences(this.context)
                                .getString("DEVICE_ADDRESS", null)
                        if (saved == address) {
                            dismiss()
//                            snackbar(this.stage,
//                                    getString(R.string.text_bluetooth_found_ko))
                        }
                    }
                }.show(supportFragmentManager, "BluetoothDialog")
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun checkBluetooth(activity: Activity): Boolean {
        val adapter = BluetoothAdapter.getDefaultAdapter()
        adapter?.let {
            return if (!it.isEnabled) {
                val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
                activity.startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT)
                false
            } else true
        }
        return false
    }
}
