package sh.zachwal.dailygames.results.resultinfo

object GeocirclesInfo : ResultInfo() {
    // Must override this because, in deserialization, a new instance is created
    override fun equals(other: Any?): Boolean {
        return other is GeocirclesInfo
    }
}

data class EnhancedGeocirclesInfo(
    val grid: String? = null,
    val numericScore: Double? = null,
    val rank: String? = null,
    val performanceDescription: String? = null,
    val boardNumber: Int? = null,
    val infinityModeOff: Boolean? = null,
    val rocketCount: Int = 0
) : ResultInfo()
