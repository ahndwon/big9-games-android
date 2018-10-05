package kr.co.big9.games.model

import android.util.Log
import com.unity3d.player.UnityPlayer
import kr.co.big9.games.listener.OnGameFinishListener
import kr.co.big9.games.listener.OnMotionListener
import kr.co.big9.games.utils.GameCountHelper
import kr.co.big9.games.utils.GameCounter


class GameManager {

    companion object {
        val TAG: String = GameManager::class.java.name
    }

    lateinit var gameCounter: GameCounter
    lateinit var finishListener: OnGameFinishListener

    fun countExercise() {
        gameCounter.counting()
    }

}