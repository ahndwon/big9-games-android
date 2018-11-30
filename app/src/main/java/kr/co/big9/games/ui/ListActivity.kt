package kr.co.big9.games.ui

import android.annotation.SuppressLint
import android.app.Activity
import android.app.ProgressDialog
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.Intent
import android.os.Bundle
import android.preference.PreferenceManager
import android.support.constraint.ConstraintLayout
import android.support.design.widget.TabLayout
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.*
import android.widget.TextView
import kotlinx.android.synthetic.main.activity_list_tab.*
import kotlinx.android.synthetic.main.fragment_bluetooth_dialog.view.*
import kotlinx.android.synthetic.main.item_device.view.*
import kr.co.big9.games.R
import kr.co.big9.games.bluetooth.BLEManager
import kr.co.big9.games.listener.BLEManagerListener
import kr.co.big9.games.ui.adapter.PagerAdapter
import kr.co.big9.games.ui.fragment.BombFragment
import kr.co.big9.games.ui.fragment.BreakBrickFragment
import kr.co.big9.games.ui.fragment.RoadFragment
import kr.co.big9.games.ui.fragment.SpaceFragment
import kr.co.big9.games.utils.*
import org.jetbrains.anko.design.snackbar
import org.jetbrains.anko.indeterminateProgressDialog

class ListActivity : AppCompatActivity() {
    companion object {
        const val REQUEST_ENABLE_BT = 333
        val TAG: String = ListActivity::class.java.name
    }

    private val bleManager = BLEManager()
    var isShowing = false
    lateinit var bluetoothDialog: ProgressDialog

    private val gameFragments = listOf(BombFragment(),
            BreakBrickFragment(),
            RoadFragment())


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_list_tab)
        setSupportActionBar(toolbar)
        supportActionBar?.let {
            title = ""
        }

//        val fragment = BombFragment()
//
//        supportFragmentManager.beginTransaction().replace(R.id.container, fragment)
//                .commit()

        setupCustomTabs()

        val fragmentAdapter = PagerAdapter(supportFragmentManager)

        addFragments(fragmentAdapter)
        gameViewPager.adapter = fragmentAdapter

        gameViewPager
                .addOnPageChangeListener(TabLayout
                        .TabLayoutOnPageChangeListener(gameTabs))

        setSelectedTab()

        gameTabs.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabReselected(tab: TabLayout.Tab?) {

            }

            override fun onTabUnselected(tab: TabLayout.Tab) {
                when (tab.position) {
                    0 -> tab.customView?.findViewById<TextView>(R.id.bombTabTextView)
                            ?.setTextColor(ContextCompat.getColor(this@ListActivity,
                                    R.color.dark_gray))
                    1 -> tab.customView?.findViewById<TextView>(R.id.normalTabTextView)
                            ?.setTextColor(ContextCompat.getColor(this@ListActivity,
                                    R.color.dark_gray))
                    2 -> tab.customView?.findViewById<TextView>(R.id.roadTabTextView)
                            ?.setTextColor(ContextCompat.getColor(this@ListActivity,
                                    R.color.dark_gray))
                    else -> {
                    }
                }
            }

            override fun onTabSelected(tab: TabLayout.Tab) {
                gameViewPager.setCurrentItem(tab.position, true)
                when (tab.position) {
                    0 -> tab.customView?.findViewById<TextView>(R.id.bombTabTextView)
                            ?.setTextColor(ContextCompat.getColor(this@ListActivity,
                                    R.color.text_black))
                    1 -> tab.customView?.findViewById<TextView>(R.id.normalTabTextView)
                            ?.setTextColor(ContextCompat.getColor(this@ListActivity,
                                    R.color.text_black))
                    2 -> tab.customView?.findViewById<TextView>(R.id.roadTabTextView)
                            ?.setTextColor(ContextCompat.getColor(this@ListActivity,
                                    R.color.text_black))
                    else -> {
                    }
                }
            }

        })

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

    private fun addFragments(fragmentAdapter: PagerAdapter) {
        for (fragment in gameFragments) {
            fragmentAdapter.addFragment(fragment, fragment::class.java.name)
        }
    }

    private fun setSelectedTab() {
        gameTabs.getTabAt(0)?.customView?.findViewById<TextView>(R.id.bombTabTextView)
                ?.setTextColor(ContextCompat.getColor(this, R.color.text_black))

        gameTabs.getTabAt(1)?.customView?.findViewById<TextView>(R.id.normalTabTextView)
                ?.setTextColor(ContextCompat.getColor(this, R.color.dark_gray))

        gameTabs.getTabAt(2)?.customView?.findViewById<TextView>(R.id.normalTabTextView)
                ?.setTextColor(ContextCompat.getColor(this, R.color.dark_gray))

        gameTabs.getTabAt(3)?.customView?.findViewById<TextView>(R.id.roadTabTextView)
                ?.setTextColor(ContextCompat.getColor(this, R.color.dark_gray))
    }

    @SuppressLint("InflateParams")
    private fun setupCustomTabs() {

        val firstTab = LayoutInflater.from(this)
                .inflate(R.layout.tab_first, null).apply {
                    layoutParams = (ConstraintLayout.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.MATCH_PARENT))
                    setPadding(0, 0, 0, 0)
                }

        val lastTab = LayoutInflater.from(this)
                .inflate(R.layout.tab_last, null).apply {
                    layoutParams = (ConstraintLayout.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.MATCH_PARENT))
                    setPadding(0, 0, 0, 0)
                }

        val normalTab = LayoutInflater.from(this)
                .inflate(R.layout.tab_normal, null).apply {
                    layoutParams = (ConstraintLayout.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.MATCH_PARENT))
                }

        gameTabs.addTab(gameTabs.newTab().setCustomView(firstTab))
        for (i in 0.until(gameFragments.size - 2)) {
            gameTabs.addTab(gameTabs.newTab().setCustomView(normalTab))
        }
        gameTabs.addTab(gameTabs.newTab().setCustomView(lastTab))
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
