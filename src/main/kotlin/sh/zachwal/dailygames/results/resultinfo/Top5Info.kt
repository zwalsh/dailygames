package sh.zachwal.dailygames.results.resultinfo

data class Top5Info constructor(
    val numGuesses: Int,
    val numCorrect: Int,
    val isPerfect: Boolean,
) : ResultInfo()
