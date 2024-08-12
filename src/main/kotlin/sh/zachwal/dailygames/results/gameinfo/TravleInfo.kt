package sh.zachwal.dailygames.results.gameinfo

data class TravleInfo constructor(
    val puzzleNumber: Int,
    val score: Int,
    val shareTextNoLink: String,
    val numGuesses: Int,
    val numIncorrect: Int,
    val numPerfect: Int,
    val numHints: Int,
)
