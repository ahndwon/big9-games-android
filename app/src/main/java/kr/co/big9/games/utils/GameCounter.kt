package kr.co.big9.games.utils

import android.util.Log
import kr.co.big9.games.listener.OnMotionListener


class GameCounter {

    companion object {
        val TAG: String = GameCounter::class.java.name
    }
    internal lateinit var countHelper: GameCountHelper
    internal lateinit var onMotionListener: OnMotionListener

    fun counting() {
        with(countHelper) {
            val leftMotion = isLeftArmStretch
            val rightMotion = isRightArmStretch

            if(leftMotion) {
                Log.d(TAG,"leftMotion")
                onMotionListener.onLeft()
            }

            if (rightMotion) {
                Log.d(TAG,"rightMotion")
                onMotionListener.onRight()
            }
        }
    }
}