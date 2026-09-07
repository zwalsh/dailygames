package sh.zachwal.dailygames.results.gamemapper

import sh.zachwal.dailygames.db.jdbi.User
import sh.zachwal.dailygames.db.jdbi.puzzle.Game
import sh.zachwal.dailygames.db.jdbi.puzzle.PuzzleResult
import sh.zachwal.dailygames.leaderboard.PointCalculator
import sh.zachwal.dailygames.results.resultinfo.FramedInfo
import sh.zachwal.dailygames.results.resultinfo.ParsedResult
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FramedMapper @Inject constructor(
    private val pointCalculator: PointCalculator,
) : GameMapper {
    override val game = Game.FRAMED

    private val framedRegex = Regex(
        """
            \s*Framed #(?<puzzleNumber>\d+)[\s\S]*
        """.trimIndent(),
    )
    private val redSquareRegex = Regex("🟥")

    override fun matches(shareText: String) = framedRegex.matches(shareText)

    override fun extract(shareText: String, user: User): ParsedResult {
        val match = framedRegex.find(shareText) ?: throw IllegalArgumentException("Share text is not a Framed share")
        val (puzzleNumber) = match.destructured
        val incorrectGuesses = redSquareRegex.findAll(shareText).count()
        val score = incorrectGuesses + 1
        return ParsedResult(
            puzzleNumber = puzzleNumber.toInt(),
            game = Game.FRAMED,
            date = null,
            score = score,
            shareTextNoLink = shareText.substringBefore("https://").trim(),
            resultInfo = FramedInfo,
        )
    }

    override fun shareLine(result: PuzzleResult): String = result.toStandardShareLine(pointCalculator)
}
