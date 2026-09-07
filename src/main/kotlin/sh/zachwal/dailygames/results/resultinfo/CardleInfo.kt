package sh.zachwal.dailygames.results.resultinfo

data class CardleInfo(
    val numGuesses: Int,
    val streak: Int,
) : ResultInfo()
