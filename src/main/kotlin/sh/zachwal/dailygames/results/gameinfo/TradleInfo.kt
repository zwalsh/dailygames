package sh.zachwal.dailygames.results.gameinfo

data class TradleInfo constructor(
    val puzzleNumber: Int,
    val score: Int,
    val shareTextNoLink: String,
) : GameInfo()
