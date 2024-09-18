package sh.zachwal.dailygames.leaderboard

import sh.zachwal.dailygames.db.jdbi.puzzle.FlagleResult
import sh.zachwal.dailygames.db.jdbi.puzzle.GeocirclesResult
import sh.zachwal.dailygames.db.jdbi.puzzle.PinpointResult
import sh.zachwal.dailygames.db.jdbi.puzzle.PuzzleResult
import sh.zachwal.dailygames.db.jdbi.puzzle.Top5Result
import sh.zachwal.dailygames.db.jdbi.puzzle.TradleResult
import sh.zachwal.dailygames.db.jdbi.puzzle.TravleResult
import sh.zachwal.dailygames.db.jdbi.puzzle.WorldleResult

class PuzzleResultPointCalculator {

    /**
     * Returns a number of points for the given puzzle result, where a positive number is always better.
     *
     * This is useful for sorting on leaderboards.
     */
    fun calculatePoints(result: PuzzleResult): Int {
        return when (result) {
            is WorldleResult -> result.calculatePoints()
            is FlagleResult -> result.calculatePoints()
            is TradleResult -> result.calculatePoints()
            is TravleResult -> result.calculatePoints()
            is Top5Result -> result.calculatePoints()
            is PinpointResult -> result.calculatePoints()
            is GeocirclesResult -> TODO()
        }
    }

    private fun WorldleResult.calculatePoints(): Int {
        return 7 - score
    }

    private fun FlagleResult.calculatePoints(): Int {
        return 7 - score
    }

    private fun TradleResult.calculatePoints(): Int {
        return 7 - score
    }

    private fun PinpointResult.calculatePoints(): Int {
        return 6 - score
    }

    /**
     * Travle's points system works as follows:
     *
     * In each Puzzle, there is a shortest possible solution. For a given shortest solution length, there is some number
     * of allowed extra guesses. The points obtained are the number of incorrect guesses left, plus one.
     *
     * For example, for a perfect result of length 7, the player is allowed 6 extra guesses, so the points are 7.
     *
     * For a result where the shortest solution is three, the player is allowed four extra guesses. If they used four
     * total guesses, their score would be 4 (3 incorrect guesses left + 1).
     *
     * See https://travle.earth/extra_info
     */
    private fun TravleResult.calculatePoints(): Int {
        if (score < 0) {
            return 0
        }
        val shortestSolutionLength = numGuesses - numIncorrect
        val allowedIncorrectGuesses = when (shortestSolutionLength) {
            in Int.MIN_VALUE..2 -> throw IllegalArgumentException("Shortest solution length must be at least 3 but was $shortestSolutionLength for result $shareText")
            3 -> 4
            in 4..6 -> 5
            in 7..9 -> 6
            in 10..12 -> 7
            in 13..Int.MAX_VALUE -> 8
            else -> throw IllegalArgumentException("Impossible shortest solution length: $shortestSolutionLength for result $shareText")
        }

        return allowedIncorrectGuesses - numIncorrect + 1
    }

    private fun Top5Result.calculatePoints(): Int {
        return score
    }
}
