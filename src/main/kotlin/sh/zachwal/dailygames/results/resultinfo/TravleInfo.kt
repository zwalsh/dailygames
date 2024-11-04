package sh.zachwal.dailygames.results.resultinfo

data class TravleInfo constructor(
    val numGuesses: Int,
    val numIncorrect: Int,
    val numPerfect: Int,
    val numHints: Int,
) : ResultInfo()
