package sh.zachwal.dailygames.results.resultinfo

data class WorldleInfo constructor(
    val percentage: Int, // TODO db constraint on percentage
) : ResultInfo()
