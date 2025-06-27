package sh.zachwal.dailygames.results.resultinfo

data class GeoGridInfo(
    val score: Double,
    val rank: Int,
    val rankOutOf: Int,
    val numCorrect: Int,
    val grid: String? = null,
    val performanceDescription: String? = null,
    val infinityModeOff: Boolean? = null,
    val rocketCount: Int = 0
) : ResultInfo()
