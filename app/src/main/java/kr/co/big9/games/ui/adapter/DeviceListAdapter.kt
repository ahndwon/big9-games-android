package kr.co.big9.games.ui.adapter

import android.bluetooth.BluetoothDevice
import android.support.v4.content.ContextCompat
import android.support.v7.widget.RecyclerView
import kr.co.big9.games.R
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.item_device.view.*
import kr.co.big9.games.ui.viewholder.DeviceViewHolder

class DeviceListAdapter : RecyclerView.Adapter<DeviceViewHolder>() {
    private var selectedDevicePosition: Int = -1
    var deviceList: Map<String, BluetoothDevice> = emptyMap()
    var onClick: ((View) -> Unit)? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DeviceViewHolder =
            DeviceViewHolder(parent)

    override fun getItemCount(): Int = deviceList.count()

    override fun onBindViewHolder(holder: DeviceViewHolder, position: Int) {
        if(selectedDevicePosition == position) {
            setClicked(holder.itemView)
        } else holder.itemView.background = null
        val keys = ArrayList(deviceList.keys)
        val address = keys[position]
        val device = deviceList[address]
        with(holder.itemView) {
            deviceAddress.text = address
            deviceName.text = device?.name
            val stateText = when(device?.bondState) {
                12 -> "페어링됨"
                11 -> "페어링중"
                     else -> "연결 가능"
            }
            deviceStateTextView.text = stateText
            setOnClickListener {
                onClick?.invoke(this)
                selectedDevicePosition = position
                notifyDataSetChanged()
            }
        }
    }

    private fun setClicked(view: View) {
        view.background = ContextCompat.getDrawable(view.context, R.drawable.bg_yellow_cornered)
    }
}