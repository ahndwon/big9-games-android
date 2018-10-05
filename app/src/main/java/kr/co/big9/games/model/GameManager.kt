package kr.co.big9.games.model

import android.content.res.Resources
import android.util.Log
import com.unity3d.player.UnityPlayer
import kr.co.big9.games.R
import kr.co.big9.games.listener.OnGameFinishListener
import kr.co.big9.games.listener.OnMotionListener
import kr.co.big9.games.utils.GameCountHelper
import kr.co.big9.games.utils.GameCounter
import java.util.*


class GameManager(val time: Int, val resources: Resources) {

    companion object {
        private const val EXERCISE_COUNT = 10
        val TAG: String = GameManager::class.java.name
    }

    private var playTime = 3000f
    private var timerTask: TimerTask? = null
    private var timer: Timer? = null

    private var canCounting: Boolean = false
    private var isSuccess: Boolean = false

    val gameCounter: GameCounter = GameCounter()
    var finishListener: OnGameFinishListener? = null
    var exerciseCountHelper: GameCountHelper? = null
        set(helper) {
            field = helper
//            exerciseCounter.setCountHelper(helper)
            helper?.let {
                gameCounter.countHelper = helper
            }
        }

    init {
        initTimer()
        gameCounter.onMotionListener = (object : OnMotionListener{
            override fun onRight() {
                Log.d(TAG, "RIGHT")
                UnityPlayer.UnitySendMessage("BLEBridge",  "OnReceiveMotion",  "RIGHT")
            }

            override fun onLeft() {
                Log.d(TAG, "LEFT")
                UnityPlayer.UnitySendMessage("BLEBridge",  "OnReceiveMotion",  "LEFT")
            }
        })
    }

    private fun initTimer() {
        Log.d(TAG, "initTimer")
        timer = Timer()
        UnityPlayer.UnitySendMessage("Player", "SetProgressMax", 3.toString())

        timerTask = object : TimerTask() {
            override fun run() {
//                println("Run")
//                if (animationCount == 3) {
//                    playTime = time / 2f
//                    UnityPlayer.UnitySendMessage("Player", "SetAnimationSpeed", 2.toString())
//                    timer?.schedule(timerTask2, 0, playTime.toLong())
//
//                    timerTask?.cancel()
//                    gameCounter.resetCount()
//                    animationCount = 0
//                    canCounting = true
//                    UnityPlayer.UnitySendMessage("Player", "SetProgressMax", EXERCISE_COUNT.toString())
//                    UnityPlayer.UnitySendMessage("Player", "SetProgress", "0")
//                    return
//                }
//                isSuccess = false
//                exerciseCounter.setExerciseLimitTime(time - 500f)
//                gameCounter.exerciseLimitTime = (time - 500f)

            }
        }
    }

    fun countExercise() {
        gameCounter.counting()
    }

    fun startExercise() {
//        timer?.schedule(timerTask, 3000, time.toLong())
    }

    fun animationEnd() {
        println("TEST End1")
    }
}