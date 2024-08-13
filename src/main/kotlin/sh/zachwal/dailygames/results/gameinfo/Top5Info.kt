package sh.zachwal.dailygames.results.gameinfo

data class Top5Info constructor(
    val puzzleNumber: Int,
    val score: Int,
    val shareTextNoLink: String,
    val numGuesses: Int,
    val numCorrect: Int,
    val isPerfect: Boolean,
)
