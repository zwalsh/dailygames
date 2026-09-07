package sh.zachwal.dailygames.results.gamemapper

import sh.zachwal.dailygames.db.jdbi.User
import sh.zachwal.dailygames.db.jdbi.puzzle.Game
import sh.zachwal.dailygames.db.jdbi.puzzle.PuzzleResult
import sh.zachwal.dailygames.leaderboard.PointCalculator
import sh.zachwal.dailygames.results.resultinfo.ParsedResult
import sh.zachwal.dailygames.results.resultinfo.WorldleInfo
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WorldleMapper @Inject constructor(
    private val pointCalculator: PointCalculator,
) : GameMapper {
    override val game = Game.WORLDLE

    private val worldleRegex = Regex(
        """\s*#Worldle\s+#(?<puzzleNumber>\d+)\s+\((?<day>\d{2})\.(?<month>\d{2})\.(?<year>\d{4})\)\s+(?<score>\S)/6\s+\((?<percentage>\d+)%\)[\s\S]*""",
    )

    override fun matches(shareText: String) = worldleRegex.matches(shareText)

    override fun extract(shareText: String, user: User): ParsedResult {
        val match = worldleRegex.find(shareText) ?: throw IllegalArgumentException("Share text is not a Worldle share")
        val (puzzleNumber, day, month, year, score, percentage) = match.destructured
        val worldleInfo = WorldleInfo(
            percentage = percentage.toInt(),
        )
        return ParsedResult(
            puzzleNumber = puzzleNumber.toInt(),
            game = Game.WORLDLE,
            date = LocalDate.of(year.toInt(), month.toInt(), day.toInt()),
            score = score.toIntOrNull() ?: 7, // X / 6 scored as 7 points
            shareTextNoLink = shareText.substringBefore("https://").trim(),
            resultInfo = worldleInfo,
        )
    }

    override fun shareLine(result: PuzzleResult): String = with(result) {
        val worldleInfo = info<WorldleInfo>()
        if (score == 7) {
            return "${game.emoji()} ${game.displayName()} #$puzzleNumber X/6 (${worldleInfo.percentage}%)"
        }
        val line = "${game.emoji()} ${game.displayName()} #$puzzleNumber $score/6"
        return if (score == 1) {
            "$line ${game.perfectEmoji()}"
        } else {
            line
        }
    }
}
