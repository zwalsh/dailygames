package sh.zachwal.dailygames.home

import sh.zachwal.dailygames.db.jdbi.puzzle.FlagleResult
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
            is FlagleResult -> TODO()
            is Top5Result -> TODO()
            is TradleResult -> puzzleResult.toShareLine()
            is TravleResult -> puzzleResult.toShareLine()
            is WorldleResult -> puzzleResult.toShareLine()
        }
    }

    private fun WorldleResult.toShareLine(): String {
        if (score == 7) {
            return "${game.emoji()} Worldle #$puzzleNumber X/6 ($scorePercentage%)"
        }
        return "${game.emoji()} Worldle #$puzzleNumber $score/6"
    }

    private fun TradleResult.toShareLine(): String {
        if (score == 7) {
            return "${game.emoji()} Tradle #$puzzleNumber X/6"
        }
        return "${game.emoji()} Tradle #$puzzleNumber $score/6"
    }

    private fun TravleResult.toShareLine(): String {
        val gameAndPuzzle = "${game.emoji()} Travle #$puzzleNumber"
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
}