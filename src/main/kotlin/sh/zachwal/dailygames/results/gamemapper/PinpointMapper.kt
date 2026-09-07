package sh.zachwal.dailygames.results.gamemapper

import sh.zachwal.dailygames.db.jdbi.User
import sh.zachwal.dailygames.db.jdbi.puzzle.Game
import sh.zachwal.dailygames.db.jdbi.puzzle.PuzzleResult
import sh.zachwal.dailygames.leaderboard.PointCalculator
import sh.zachwal.dailygames.results.resultinfo.ParsedResult
import sh.zachwal.dailygames.results.resultinfo.PinpointInfo
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PinpointMapper @Inject constructor(
    private val pointCalculator: PointCalculator,
) : GameMapper {
    override val game = Game.PINPOINT

    private val pinpointRegex = Regex(
        """
            \s*Pinpoint #(?<puzzleNumber>\d+)[\s\S]*\((?<score>\S)/5\)[\s\S]*
        """.trimIndent(),
    )

    override fun matches(shareText: String) = pinpointRegex.matches(shareText)

    override fun extract(shareText: String, user: User): ParsedResult {
        val match =
            pinpointRegex.find(shareText) ?: throw IllegalArgumentException("Share text is not a Pinpoint share")
        val (puzzleNumber, score) = match.destructured
        return ParsedResult(
            puzzleNumber = puzzleNumber.toInt(),
            game = Game.PINPOINT,
            date = null,
            score = score.toIntOrNull() ?: 6, // X / 5 scored as 6 points
            shareTextNoLink = shareText.substringBefore("lnkd").trim(),
            resultInfo = PinpointInfo,
        )
    }

    override fun shareLine(result: PuzzleResult): String = result.toStandardShareLine(pointCalculator)
}
