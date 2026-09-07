package sh.zachwal.dailygames.results.gamemapper

import sh.zachwal.dailygames.db.jdbi.User
import sh.zachwal.dailygames.db.jdbi.puzzle.Game
import sh.zachwal.dailygames.db.jdbi.puzzle.PuzzleResult
import sh.zachwal.dailygames.leaderboard.PointCalculator
import sh.zachwal.dailygames.results.resultinfo.BandleInfo
import sh.zachwal.dailygames.results.resultinfo.ParsedResult
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BandleMapper @Inject constructor(
    private val pointCalculator: PointCalculator,
) : GameMapper {
    override val game = Game.BANDLE

    private val skipRegex = Regex("⬛")
    private val correctBandRegex = Regex("🟨")
    private val incorrectRegex = Regex("🟥")

    override fun matches(shareText: String) = shareText.trim().startsWith("Bandle")

    override fun extract(shareText: String, user: User): ParsedResult {
        val puzzleNumber = shareText
            .substringAfter("Bandle #")
            .substringBefore(" ")
            .trim()
            .toInt()

        val scoreText = shareText
            .trim()
            .lines()
            .first()
            .substringBefore("/")
            .substringAfterLast(" ")
        val score = if (scoreText == "x") 7 else scoreText.toInt()

        return ParsedResult(
            puzzleNumber = puzzleNumber,
            game = Game.BANDLE,
            date = null,
            score = score,
            shareTextNoLink = shareText.substringBefore("Found").trim(),
            resultInfo = BandleInfo(
                numSkips = skipRegex.findAll(shareText).count(),
                numCorrectBand = correctBandRegex.findAll(shareText).count(),
                numIncorrect = incorrectRegex.findAll(shareText).count(),
            ),
        )
    }

    override fun shareLine(result: PuzzleResult): String = result.toStandardShareLine(pointCalculator)
}
