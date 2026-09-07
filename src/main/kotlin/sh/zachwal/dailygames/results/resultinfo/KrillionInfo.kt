package sh.zachwal.dailygames.results.resultinfo

data class KrillionInfo(
    val rawScore: Int,
    val missCount: Int,
    val planktonCount: Int,
    val schoolerCount: Int,
    val rareCount: Int,
    val deepCutCount: Int,
    val krillionCount: Int,
) : ResultInfo()
