package sh.zachwal.dailygames.results.gameinfo

data class PinpointInfo constructor(
    val puzzleNumber: Int,
    val score: Int,
    val shareTextNoLink: String,
) : GameInfo()
