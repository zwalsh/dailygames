package sh.zachwal.dailygames.results.resultinfo

data class GeoGridInfo(
    val score: Double,
    val rank: Int,
    val rankOutOf: Int,
    val numCorrect: Int,
) : ResultInfo()
