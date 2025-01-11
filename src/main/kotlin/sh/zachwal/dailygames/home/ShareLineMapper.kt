package sh.zachwal.dailygames.home

import sh.zachwal.dailygames.db.jdbi.puzzle.Game
import sh.zachwal.dailygames.db.jdbi.puzzle.PuzzleResult
import sh.zachwal.dailygames.leaderboard.PointCalculator
import sh.zachwal.dailygames.results.resultinfo.Top5Info
import sh.zachwal.dailygames.results.resultinfo.TravleInfo
import sh.zachwal.dailygames.results.resultinfo.WorldleInfo
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ShareLineMapper @Inject constructor(
    private val pointCalculator: PointCalculator
) {

    fun mapToShareLine(result: PuzzleResult): String {
        return when (result.game) {
            Game.FLAGLE,
            Game.TRADLE,
            Game.FRAMED,
            Game.PINPOINT -> result.toStandardShareLine()

            Game.GEOCIRCLES -> result.toGeocirclesShareLine()
            Game.TOP5 -> result.toTop5ShareLine()
            Game.WORLDLE -> result.toWorldleShareLine()
            Game.TRAVLE -> result.toTravleShareLine()
            Game.GEOGRID -> TODO()
        }
    }

    private fun PuzzleResult.toStandardShareLine(): String {
        val gameAndPuzzle = "${game.emoji()} ${game.displayName()} #$puzzleNumber"
        val maxPoints = pointCalculator.maxPoints(this)
        if (score == maxPoints + 1) {
            return "$gameAndPuzzle X/$maxPoints"
        }
        val line = "$gameAndPuzzle $score/$maxPoints"
        return if (score == 1) {
            "$line ${game.perfectEmoji()}"
        } else {
            line
        }
    }

    private fun PuzzleResult.toGeocirclesShareLine(): String {
        val start = "${game.emoji()} ${game.displayName()} #$puzzleNumber"

        return when (score) {
            10 -> "$start 5/5 ${game.perfectEmoji()}"
            in (5..9) -> "$start 5/5 (${10 - score} wrong)"
            else -> "$start $score/5"
        }
    }

    private fun PuzzleResult.toTop5ShareLine(): String = with(info<Top5Info>()) {
        val numIncorrect = numGuesses - numCorrect
        val start = "${game.emoji()} ${game.displayName()} #$puzzleNumber $numCorrect/5"

        return if (isPerfect) {
            "$start ${game.perfectEmoji()}"
        } else if (numIncorrect > 0 && numCorrect == 5) {
            "$start ($numIncorrect wrong)"
        } else {
            start
        }
    }

    private fun PuzzleResult.toWorldleShareLine(): String = with(info<WorldleInfo>()) {
        if (score == 7) {
            return "${game.emoji()} ${game.displayName()} #$puzzleNumber X/6 ($percentage%)"
        }
        val line = "${game.emoji()} ${game.displayName()} #$puzzleNumber $score/6"
        return if (score == 1) {
            "$line ${game.perfectEmoji()}"
        } else {
            line
        }
    }

    private fun PuzzleResult.toTravleShareLine(): String = with(info<TravleInfo>()) {
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
            "$withHints ${game.perfectEmoji()}"
        } else {
            withHints
        }

        return withPerfect
    }
}
