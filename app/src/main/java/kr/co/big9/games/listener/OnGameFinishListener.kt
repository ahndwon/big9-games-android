package kr.co.big9.games.listener

interface OnGameFinishListener {
    fun finishExercise(star: Int, score: Int, maxCount: Int)
}