package sh.zachwal.dailygames.results.gameinfo

data class GeocirclesInfo constructor(
    val puzzleNumber: Int,
    val score: Int,
    val shareTextNoLink: String,
) : GameInfo()
