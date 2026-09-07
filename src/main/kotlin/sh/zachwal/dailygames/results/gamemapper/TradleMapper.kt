package sh.zachwal.dailygames.results.gamemapper

import sh.zachwal.dailygames.db.jdbi.User
import sh.zachwal.dailygames.db.jdbi.puzzle.Game
import sh.zachwal.dailygames.db.jdbi.puzzle.PuzzleResult
import sh.zachwal.dailygames.leaderboard.PointCalculator
import sh.zachwal.dailygames.results.resultinfo.ParsedResult
import sh.zachwal.dailygames.results.resultinfo.TradleInfo
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TradleMapper @Inject constructor(
    private val pointCalculator: PointCalculator,
) : GameMapper {
    override val game = Game.TRADLE

    private val tradleRegex = Regex(
        """
            \s*#Tradle\s+#(?<puzzleNumber>\d+)\s+(?<score>\S)/6[\s\S]*
        """.trimIndent(),
    )

    override fun matches(shareText: String) = tradleRegex.matches(shareText)

    override fun extract(shareText: String, user: User): ParsedResult {
        val match = tradleRegex.find(shareText) ?: throw IllegalArgumentException("Share text is not a Tradle share")
        val (puzzleNumber, score) = match.destructured
        val tradleInfo = TradleInfo
        return ParsedResult(
            puzzleNumber = puzzleNumber.toInt(),
            game = Game.TRADLE,
            date = null,
            score = score.toIntOrNull() ?: 7, // X / 6 scored as 7 points.
            shareTextNoLink = shareText.substringBefore("https://").trim(),
            resultInfo = tradleInfo,
        )
    }

    override fun shareLine(result: PuzzleResult): String = result.toStandardShareLine(pointCalculator)
}
