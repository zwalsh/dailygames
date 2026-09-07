package sh.zachwal.dailygames.results.gamemapper

import sh.zachwal.dailygames.db.jdbi.User
import sh.zachwal.dailygames.db.jdbi.puzzle.Game
import sh.zachwal.dailygames.db.jdbi.puzzle.PuzzleResult
import sh.zachwal.dailygames.results.resultinfo.ParsedResult
import sh.zachwal.dailygames.results.resultinfo.Top5Info
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class Top5Mapper @Inject constructor() : GameMapper {
    override val game = Game.TOP5

    private val top5Regex = Regex(
        """
            \s*Top 5\s+#(?<puzzleNumber>\d+)[\s\S]*
        """.trimIndent(),
    )
    private val perfectTop5Regex = Regex("🟥🟧🟨🟩🟦")
    private val top5GuessRegex = Regex("[🟥🟧🟨🟩🟦⬜]")
    private val top5CorrectRegex = Regex("[[🟥🟧🟨🟩🟦]]")

    override fun matches(shareText: String) = top5Regex.matches(shareText)

    override fun extract(shareText: String, user: User): ParsedResult {
        val match = top5Regex.find(shareText) ?: throw IllegalArgumentException("Share text is not a Top 5 share")

        val (puzzleNumber) = match.destructured

        val isPerfect = perfectTop5Regex.find(shareText) != null
        val numGuesses = top5GuessRegex.findAll(shareText).count()
        val numCorrect = top5CorrectRegex.findAll(shareText).count()
        val livesAtStart = 5
        val score = livesAtStart - (numGuesses - numCorrect) + numCorrect
        val top5Info = Top5Info(
            numGuesses = numGuesses,
            numCorrect = numCorrect,
            isPerfect = isPerfect,
        )
        return ParsedResult(
            puzzleNumber = puzzleNumber.toInt(),
            game = Game.TOP5,
            date = null,
            score = score,
            shareTextNoLink = shareText.substringBefore("https://").trim(),
            resultInfo = top5Info,
        )
    }

    override fun shareLine(result: PuzzleResult): String = with(result) {
        val info = info<Top5Info>()
        val numIncorrect = info.numGuesses - info.numCorrect
        val start = "${game.emoji()} ${game.displayName()} #$puzzleNumber ${info.numCorrect}/5"

        return if (info.isPerfect) {
            "$start ${game.perfectEmoji()}"
        } else if (numIncorrect > 0 && info.numCorrect == 5) {
            "$start ($numIncorrect wrong)"
        } else {
            start
        }
    }
}
