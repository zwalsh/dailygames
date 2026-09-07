package sh.zachwal.dailygames.results.gamemapper

import sh.zachwal.dailygames.db.jdbi.User
import sh.zachwal.dailygames.db.jdbi.puzzle.Game
import sh.zachwal.dailygames.db.jdbi.puzzle.PuzzleResult
import sh.zachwal.dailygames.leaderboard.PointCalculator
import sh.zachwal.dailygames.results.resultinfo.FlagleInfo
import sh.zachwal.dailygames.results.resultinfo.ParsedResult
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FlagleMapper @Inject constructor(
    private val pointCalculator: PointCalculator,
) : GameMapper {
    override val game = Game.FLAGLE

    private val flagleRegex = Regex(
        """
            \s*#Flagle\s+#(?<puzzleNumber>\d+)\s+\((?<day>\d{2})\.(?<month>\d{2})\.(?<year>\d{4})\)\s+(?<score>\S)/6\s+[\s\S]*
        """.trimIndent(),
    )

    override fun matches(shareText: String) = flagleRegex.matches(shareText)

    override fun extract(shareText: String, user: User): ParsedResult {
        val match = flagleRegex.find(shareText) ?: throw IllegalArgumentException("Share text is not a Flagle share")
        val (puzzleNumber, day, month, year, score) = match.destructured
        return ParsedResult(
            puzzleNumber = puzzleNumber.toInt(),
            game = Game.FLAGLE,
            date = LocalDate.of(year.toInt(), month.toInt(), day.toInt()),
            score = score.toIntOrNull() ?: 7, // X / 6 scored as 7 points
            shareTextNoLink = shareText.substringBefore("https://").trim(),
            resultInfo = FlagleInfo,
        )
    }

    override fun shareLine(result: PuzzleResult): String = result.toStandardShareLine(pointCalculator)
}
