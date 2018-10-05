package kr.co.big9.games.utils

import kr.co.big9.games.listener.OnMotionListener


class GameCounter {
    internal lateinit var countHelper: GameCountHelper
    internal lateinit var onMotionListener: OnMotionListener
    internal var canCounting = true
    internal var startTime: Long = 0
    internal var exerciseLimitTime: Float = 100000f




    fun counting() {
        with(countHelper) {
            var leftMotion = isLeftArmStretch
            var rightMotion = isRightArmStretch

            if(leftMotion) {
                onMotionListener.onLeft()
                leftMotion = false
            }

            if (rightMotion) {
                onMotionListener.onRight()
                rightMotion = false
            }
        }
    }


    fun onPostureCheck() {
        countHelper.isPostureChanged = false
    }
}