package sh.zachwal.dailygames.home

import sh.zachwal.dailygames.db.jdbi.puzzle.FlagleResult
import sh.zachwal.dailygames.db.jdbi.puzzle.GeocirclesResult
import sh.zachwal.dailygames.db.jdbi.puzzle.PinpointResult
import sh.zachwal.dailygames.db.jdbi.puzzle.PuzzleResult
import sh.zachwal.dailygames.db.jdbi.puzzle.Top5Result
import sh.zachwal.dailygames.db.jdbi.puzzle.TradleResult
import sh.zachwal.dailygames.db.jdbi.puzzle.TravleResult
import sh.zachwal.dailygames.db.jdbi.puzzle.WorldleResult
import javax.inject.Singleton

@Singleton
class ShareLineMapper {

    fun mapToShareLine(puzzleResult: PuzzleResult): String {
        return when (puzzleResult) {
            is FlagleResult -> puzzleResult.toShareLine()
            is Top5Result -> puzzleResult.toShareLine()
            is TradleResult -> puzzleResult.toShareLine()
            is TravleResult -> puzzleResult.toShareLine()
            is WorldleResult -> puzzleResult.toShareLine()
            is PinpointResult -> puzzleResult.toShareLine()
            is GeocirclesResult -> puzzleResult.toShareLine()
        }
    }

    private fun GeocirclesResult.toShareLine(): String {
        val perfectEmoji = "\uD83C\uDFAF" // target emoji
        val start = "${game.emoji()} ${game.displayName()} #$puzzleNumber"

        return when (score) {
            10 -> "$start 5/5 $perfectEmoji"
            in (5..9) -> "$start 5/5 (${10 - score} wrong)"
            else -> "$start $score/5"
        }
    }

    private fun Top5Result.toShareLine(): String {
        val numIncorrect = numGuesses - numCorrect
        val start = "${game.emoji()} ${game.displayName()} #$puzzleNumber $numCorrect/5"

        return if (isPerfect) {
            "$start \uD83C\uDF08"
        } else if (numIncorrect > 0 && numCorrect == 5) {
            "$start ($numIncorrect wrong)"
        } else {
            start
        }
    }

    private fun FlagleResult.toShareLine(): String {
        if (score == 7) {
            return "${game.emoji()} ${game.displayName()} #$puzzleNumber X/6"
        }
        return "${game.emoji()} ${game.displayName()} #$puzzleNumber $score/6"
    }

    private fun WorldleResult.toShareLine(): String {
        if (score == 7) {
            return "${game.emoji()} ${game.displayName()} #$puzzleNumber X/6 ($scorePercentage%)"
        }
        return "${game.emoji()} ${game.displayName()} #$puzzleNumber $score/6"
    }

    private fun TradleResult.toShareLine(): String {
        if (score == 7) {
            return "${game.emoji()} ${game.displayName()} #$puzzleNumber X/6"
        }
        return "${game.emoji()} ${game.displayName()} #$puzzleNumber $score/6"
    }

    private fun TravleResult.toShareLine(): String {
        val gameAndPuzzle = "${game.emoji()} ${game.displayName()} #$puzzleNumber"
        val withScore = if (score < 0) {
            "$gameAndPuzzle (${-score} away)"
        } else {
            "$gameAndPuzzle +$score"
        }

        val withHints = when (numHints) {
            0 -> withScore
            1 -> "$withScore (1 hint)"
            in (2..Int.MAX_VALUE) -> "$withScore ($numHints hints)"
            else -> throw IllegalStateException("numHints should be non-negative")
        }

        val withPerfect = if (numPerfect == numGuesses) {
            "$withHints (Perfect)"
        } else {
            withHints
        }

        return withPerfect
    }

    private fun PinpointResult.toShareLine(): String {
        val gameAndPuzzle = "${game.emoji()} ${game.displayName()} #$puzzleNumber"
        if (score == 6) {
            return "$gameAndPuzzle X/5"
        }
        return "$gameAndPuzzle $score/5"
    }
}
