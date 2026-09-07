package sh.zachwal.dailygames.results.resultinfo

data class MapTapInfo(
    val finalScore: Int,
    val roundScores: List<Int>,
) : ResultInfo()
