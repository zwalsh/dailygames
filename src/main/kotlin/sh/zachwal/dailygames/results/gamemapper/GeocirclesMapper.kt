package sh.zachwal.dailygames.results.gamemapper

import sh.zachwal.dailygames.db.jdbi.User
import sh.zachwal.dailygames.db.jdbi.puzzle.Game
import sh.zachwal.dailygames.db.jdbi.puzzle.PuzzleResult
import sh.zachwal.dailygames.results.resultinfo.GeocirclesInfo
import sh.zachwal.dailygames.results.resultinfo.ParsedResult
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GeocirclesMapper @Inject constructor() : GameMapper {
    override val game = Game.GEOCIRCLES

    private val geocirclesRegex = Regex(
        """
            \s*Geocircles #(?<puzzleNumber>\d+)[\s\S]*
        """.trimIndent(),
    )
    private val greenCircleOrHeartRegex = Regex("(🟢|❤️)")

    override fun matches(shareText: String) = geocirclesRegex.matches(shareText)

    override fun extract(shareText: String, user: User): ParsedResult {
        val match =
            geocirclesRegex.find(shareText) ?: throw IllegalArgumentException("Share text is not a Geocircles share")
        val (puzzleNumber) = match.destructured
        val score = greenCircleOrHeartRegex.findAll(shareText).count()
        return ParsedResult(
            puzzleNumber = puzzleNumber.toInt(),
            game = Game.GEOCIRCLES,
            date = null,
            score = score,
            shareTextNoLink = shareText.substringBefore("https://").trim(),
            resultInfo = GeocirclesInfo,
        )
    }

    override fun shareLine(result: PuzzleResult): String = with(result) {
        val start = "${game.emoji()} ${game.displayName()} #$puzzleNumber"

        return when (score) {
            10 -> "$start 5/5 ${game.perfectEmoji()}"
            in (5..9) -> "$start 5/5 (${10 - score} wrong)"
            else -> "$start $score/5"
        }
    }
}
