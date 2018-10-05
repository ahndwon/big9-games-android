package kr.co.big9.games.listener

interface OnProtocolListener {
    // 0 - voltage, lowBattery
    // 1 - sideAngle, frontAngle
    // 2 - leftUpSwitch, rightUpSwitch
    // 3 - leftDistance, rightDistance
    // 4 - coinFlag, charging(4.2 - 100%)
    fun onVoltage(voltage: Double, isLowBattery: Int)
    fun onAngle(angleSide: Double, angleFront: Double)
    fun onUpper(isLeftUp: Int, isRightUp: Int)
    fun onPull(left: Int, right: Int)
    fun onCoin(coin: Int, isCharging: Int)
    fun processData(packet: String)
}