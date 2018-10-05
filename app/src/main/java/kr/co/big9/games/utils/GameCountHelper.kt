package kr.co.big9.games.utils

import android.util.Log
import kr.co.big9.games.listener.OnProtocolListener


class GameCountHelper : OnProtocolListener {

    companion object {
        val TAG: String = GameCountHelper::class.java.name
        const val LEFT_TILT_THRESHOLD = -20
        const val RIGHT_TILT_THRESHOLD = 20
        const val FRONT_TILT_THRESHOLD = -50
        const val BACK_TILT_THRESHOLD = 10
        const val ARM_STRETCH_THRESHOLD = 4
        const val POSTURE_THRESHOLD = 20
//        const val STAND_UP_THRESHOLD = 30
    }

    var isRightArmStretch = false
        private set

    var isRightArmUp = false
        private set

    var isLeftArmStretch = false
        private set

    var isLeftArmUp = false
        private set

    var isPostureChanged = false

    var isSitDown = false
        private set

    var isStandUp = true
        private set

    var isLegDown: Boolean = false
        private set
    var isLegUp: Boolean = false
        private set
    var isRotateRight: Boolean = false
        private set
    var isRotateLeft: Boolean = false
        private set
    var isTiltFront: Boolean = false
        private set
    var isTiltBack: Boolean = false
        private set
    var isTiltRight: Boolean = false
        private set
    var isTiltLeft: Boolean = false
        private set

    var leftTiltThreshold = LEFT_TILT_THRESHOLD
    var rightTiltThreshold = RIGHT_TILT_THRESHOLD
    var frontTiltThreshold = FRONT_TILT_THRESHOLD
    var backTiltThreshold = BACK_TILT_THRESHOLD
    var postureThreshold = POSTURE_THRESHOLD
    var armStretchThreshold = ARM_STRETCH_THRESHOLD

    private var angleFront = 0.toDouble()
    private var angleSide = 0.toDouble()


    override fun onVoltage(voltage: Double, isLowBattery: Int) {
//        Log.i(BluetoothActivity.TAG, "voltage: $voltage isLowBattery: $isLowBattery")
    }

    override fun onAngle(angleSide: Double, angleFront: Double) {
//        Log.i(BluetoothActivity.TAG, "sideAngle: $sideAngle frontAngle: $frontAngle")

        val diffSide = this.angleSide - angleSide
        val diffFront = this.angleFront - angleFront
        this.angleSide = angleSide
        this.angleFront = angleFront


        // 앉았다 일어났다
        if (Math.abs(diffFront) > postureThreshold) {
            Log.d(TAG, "POSTURE CHANGED")
            isPostureChanged = true
        }

        isTiltLeft = if (angleSide < leftTiltThreshold) {
            Log.d(TAG, "왼쪽으로 기울임!")
            true
        } else false

        isTiltRight = if (angleSide > rightTiltThreshold) {
            Log.d(TAG, "오른쪽으로 기울임!")
            true
        } else false

        isTiltFront = if (angleFront < frontTiltThreshold) {
            Log.d(TAG, "앞으로 숙임!")
            true
        } else false

        isTiltBack = if (angleFront > backTiltThreshold) {
            Log.d(TAG, "뒤로 숙임!")
            true
        } else false
    }

    override fun onUpper(isLeftUp: Int, isRightUp: Int) {
//        Log.i(BluetoothActivity.TAG, "isLeftUp: $isLeftUp isRightUp: $isRightUp")

        isLeftArmUp = if (isLeftUp > 0) {
            Log.d(TAG, "왼쪽 위!")
            true
        } else false

        isRightArmUp = if (isRightUp > 0) {
            Log.d(TAG, "오른쪽 위!")
            true
        } else false

    }

    override fun onPull(left: Int, right: Int) {
//        Log.i(BluetoothActivity.TAG, "left: $left right: $right")

        isLeftArmStretch = if (left > armStretchThreshold) {
            Log.d(TAG, "왼쪽 땡김!")
            true
        } else {
            false
        }
        isRightArmStretch = if (right > armStretchThreshold) {
            Log.d(TAG, "오른쪽 땡김!")
            true
        } else {
            false
        }
    }

    override fun onCoin(coin: Int, isCharging: Int) {
//        Log.i(BluetoothActivity.TAG, "coin: $coin isCharging: $isCharging")
    }

    override fun processData(packet: String) {
        if (packet.isEmpty()) {
            return
        }
        // 0 - voltage, lowBattery
        // 1 - sideAngle, frontAngle
        // 2 - leftUpSwitch, rightUpSwitch
        // 3 - leftDistance, rightDistance
        // 4 - coinFlag, charging(4.2 - 100%)
        val token = packet.trim().split(",")
//        Log.i("WTF", packet)

        try {
            //            protocolText.append(packet)

            if (token.size == 3) {
                when (token[0]) {
                    "0" -> onVoltage(java.lang.Double.parseDouble(token[1]), Integer.parseInt(token[2]))
                    "1" -> onAngle(java.lang.Double.parseDouble(token[1]), java.lang.Double.parseDouble(token[2]))
                    "2" -> onUpper(Integer.parseInt(token[1]), Integer.parseInt(token[2]))
                    "3" -> onPull(Integer.parseInt(token[1]), Integer.parseInt(token[2]))
                    "4" -> onCoin(Integer.parseInt(token[1]), Integer.parseInt(token[2]))
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }


    fun resetTilt() {
        isTiltBack = false
        isTiltFront = false
        isTiltLeft = false
        isTiltRight = false
    }

    fun initArmFlag() {
        isLeftArmStretch = false
        isLeftArmUp = false
        isRightArmStretch = false
        isRightArmUp = false
//        isRightArmUpFold = true
//        isRightArmUpStretch = false
//        isRightArmDownFold = true
//        isRightArmDownStretch = false
//
//        isLeftArmUpFold = true
//        isLeftArmUpStretch = false
//        isLeftArmDownFold = true
//        isLeftArmDownStretch = false
    }

    fun initLegFlag() {
        isLegDown = false
        isLegUp = false
        isRotateLeft = false
        isRotateRight = false
        isTiltLeft = false
        isTiltRight = false
    }

}
