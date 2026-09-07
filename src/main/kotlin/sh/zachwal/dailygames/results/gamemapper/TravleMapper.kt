package sh.zachwal.dailygames.results.gamemapper

import sh.zachwal.dailygames.db.jdbi.User
import sh.zachwal.dailygames.db.jdbi.puzzle.Game
import sh.zachwal.dailygames.db.jdbi.puzzle.PuzzleResult
import sh.zachwal.dailygames.results.resultinfo.ParsedResult
import sh.zachwal.dailygames.results.resultinfo.TravleInfo
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TravleMapper @Inject constructor() : GameMapper {
    override val game = Game.TRAVLE

    private val puzzleNumberRegex = Regex("""#(?<puzzleNumber>\d+)""")
    private val scoreRegex = Regex("""\+(?<score>\d+)""")
    private val numAwayRegex = Regex("""\((?<numAway>\d+) away\)""")
    private val hintCountRegex = Regex("""\((?<hintCount>\d+) hints?\)""")
    private val guessEmojiRegex = Regex("[🟧🟩✅🟧🟥🟩✅]")
    private val incorrectGuessEmojiRegex = Regex("[🟧🟥]")
    private val checkboxEmojiRegex = Regex("✅")

    override fun matches(shareText: String) = shareText.contains("#travle")

    override fun extract(shareText: String, user: User): ParsedResult {
        if (!shareText.contains("#travle")) {
            throw IllegalArgumentException("Share text is not a Travle share")
        }

        val puzzleNumber = puzzleNumberRegex.find(shareText)?.groupValues?.get(1)
            ?: throw IllegalArgumentException("Puzzle number not found")
        val score = scoreRegex.find(shareText)?.groupValues?.get(1)?.toInt()
            // Use the number away times negative one as the score if the result is a Did Not Finish
            ?: numAwayRegex.find(shareText)?.groupValues?.get(1)?.toInt()?.times(-1)
            ?: throw IllegalArgumentException("Score not found")
        val numGuesses = guessEmojiRegex.findAll(shareText).count()
        val numIncorrect = incorrectGuessEmojiRegex.findAll(shareText).count()
        val numPerfect = checkboxEmojiRegex.findAll(shareText).count()
        val hintCount = hintCountRegex.find(shareText)?.groupValues?.get(1)?.toIntOrNull() ?: 0

        val travleInfo = TravleInfo(
            numGuesses = numGuesses,
            numIncorrect = numIncorrect,
            numPerfect = numPerfect,
            numHints = hintCount,
        )
        return ParsedResult(
            puzzleNumber = puzzleNumber.toInt(),
            game = Game.TRAVLE,
            date = null,
            score = score,
            shareTextNoLink = shareText.substringBefore("https://").trim(),
            resultInfo = travleInfo,
        )
    }

    override fun shareLine(result: PuzzleResult): String = with(result) {
        val info = info<TravleInfo>()
        val gameAndPuzzle = "${game.emoji()} ${game.displayName()} #$puzzleNumber"
        val withScore = if (score < 0) {
            "$gameAndPuzzle (${-score} away)"
        } else {
            "$gameAndPuzzle +$score"
        }

        val withHints = when (info.numHints) {
            0 -> withScore
            1 -> "$withScore (1 hint)"
            in (2..Int.MAX_VALUE) -> "$withScore (${info.numHints} hints)"
            else -> throw IllegalStateException("numHints should be non-negative")
        }

        return if (info.numPerfect == info.numGuesses) {
            "$withHints ${game.perfectEmoji()}"
        } else {
            withHints
        }
    }
}
