package sh.zachwal.dailygames.results.resultinfo

data class BandleInfo(
    val numSkips: Int,
    val numCorrectBand: Int,
    val numIncorrect: Int,
) : ResultInfo()
