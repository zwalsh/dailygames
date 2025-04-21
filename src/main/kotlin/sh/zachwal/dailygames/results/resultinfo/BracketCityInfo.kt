package sh.zachwal.dailygames.results.resultinfo

data class BracketCityInfo(
    val rank: String,
    val rankEmoji: String,
    val wrongGuesses: Int,
    val peeks: Int = 0,
    val answersRevealed: Int = 0,
    val totalScore: Double,
    val grid: String
) : ResultInfo()
