package sh.zachwal.dailygames.results.gameinfo

data class Top5Info constructor(
    val numGuesses: Int,
    val numCorrect: Int,
    val isPerfect: Boolean,
) : ResultInfo()
