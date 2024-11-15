package sh.zachwal.dailygames.leaderboard

import sh.zachwal.dailygames.db.jdbi.Result
import sh.zachwal.dailygames.db.jdbi.puzzle.FlagleResult
import sh.zachwal.dailygames.db.jdbi.puzzle.Game
import sh.zachwal.dailygames.db.jdbi.puzzle.GeocirclesResult
import sh.zachwal.dailygames.db.jdbi.puzzle.PinpointResult
import sh.zachwal.dailygames.db.jdbi.puzzle.PuzzleResult
import sh.zachwal.dailygames.db.jdbi.puzzle.Top5Result
import sh.zachwal.dailygames.db.jdbi.puzzle.TradleResult
import sh.zachwal.dailygames.db.jdbi.puzzle.TravleResult
import sh.zachwal.dailygames.db.jdbi.puzzle.WorldleResult
import sh.zachwal.dailygames.results.resultinfo.TravleInfo

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
            is GeocirclesResult -> result.score
        }
    }

    fun calculatePoints(result: Result): Int = with(result) {
        when (game) {
            Game.WORLDLE -> 7 - score
            Game.TRADLE -> 7 - score
            Game.TRAVLE -> (result.resultInfo as TravleInfo).calculatePoints(score) // TODO safe cast method on result?
            Game.TOP5 -> score
            Game.FLAGLE -> 7 - score
            Game.PINPOINT -> 6 - score
            Game.GEOCIRCLES -> 10 - score
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

    private fun TravleResult.calculatePoints(): Int {
        return TravleInfo(
            numGuesses = numGuesses,
            numIncorrect = numIncorrect,
            numPerfect = numPerfect,
            numHints = numHints,
        ).calculatePoints(score)
    }

    private fun TravleResult.maxPoints(): Int {
        return TravleInfo(
            numGuesses = numGuesses,
            numIncorrect = numIncorrect,
            numPerfect = numPerfect,
            numHints = numHints,
        ).maxPoints(score)
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
    private fun TravleInfo.calculatePoints(score: Int): Int {
        if (score < 0) {
            return 0
        }
        return maxPoints(score) - score
    }

    private fun TravleInfo.maxPoints(score: Int): Int {
        return getAllowedIncorrectGuesses(score) + 1
    }

    private fun TravleInfo.getAllowedIncorrectGuesses(score: Int): Int {
        if (score >= 0) {
            // if the score is positive, it represents the excess number of guesses the player took
            // meaning shortest solution is always numGuesses minus score
            val shortestSolutionLength = numGuesses - score
            return calculateAllowedIncorrectGuesses(shortestSolutionLength)
        } else {
            // if the score is negative, it represents how far away the player was from the solution
            // shortest solution + allowed incorrect guesses == numGuesses
            // iterate through possible shortest solution lengths until we find the correct number
            for (possibleShortestSolutionLength in (3..100)) {
                val allowedIncorrectForThisLength = calculateAllowedIncorrectGuesses(possibleShortestSolutionLength)
                if (possibleShortestSolutionLength + allowedIncorrectForThisLength == numGuesses) {
                    return allowedIncorrectForThisLength
                }
            }
            throw IllegalArgumentException("Could not calculate allowed incorrect guesses for Travle result.")
        }
    }

    private fun calculateAllowedIncorrectGuesses(shortestSolutionLength: Int): Int {
        return when (shortestSolutionLength) {
            in Int.MIN_VALUE..2 -> throw IllegalArgumentException("Shortest solution length must be at least 3 but was $shortestSolutionLength.")
            3 -> 4
            in 4..6 -> 5
            in 7..9 -> 6
            in 10..12 -> 7
            in 13..Int.MAX_VALUE -> 8
            else -> throw IllegalArgumentException("Impossible shortest solution length: $shortestSolutionLength.")
        }
    }

    private fun Top5Result.calculatePoints(): Int {
        return score
    }

    fun maxPoints(result: PuzzleResult): Int {
        return when (result) {
            is WorldleResult -> 6
            is FlagleResult -> 6
            is TradleResult -> 6
            is TravleResult -> result.maxPoints()
            is Top5Result -> 10
            is PinpointResult -> 5
            is GeocirclesResult -> 10
        }
    }

    fun maxPoints(result: Result): Int = with(result) {
        when (game) {
            Game.WORLDLE -> 6
            Game.TRADLE -> 6
            Game.TRAVLE -> (result.resultInfo as TravleInfo).maxPoints(score)
            Game.TOP5 -> 10
            Game.FLAGLE -> 6
            Game.PINPOINT -> 5
            Game.GEOCIRCLES -> 10
        }
    }
}
