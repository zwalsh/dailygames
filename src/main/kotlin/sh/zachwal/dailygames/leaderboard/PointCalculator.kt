package sh.zachwal.dailygames.leaderboard

import sh.zachwal.dailygames.db.jdbi.puzzle.Game
import sh.zachwal.dailygames.db.jdbi.puzzle.PuzzleResult
import sh.zachwal.dailygames.results.resultinfo.TravleInfo

class PointCalculator {

    /**
     * Returns a number of points for the given puzzle result, where a positive number is always better.
     *
     * This is useful for sorting on leaderboards.
     */
    fun calculatePoints(result: PuzzleResult): Int = with(result) {
        when (game) {
            Game.WORLDLE,
            Game.TRADLE,
            Game.FLAGLE,
            Game.PINPOINT,
            Game.FRAMED -> maxPoints(result) + 1 - score

            Game.TOP5,
            Game.GEOCIRCLES,
            Game.GEOGRID -> score

            Game.TRAVLE -> (result.info<TravleInfo>()).calculatePoints(score)

            Game.BANDLE -> TODO()
        }
    }

    fun maxPoints(result: PuzzleResult): Int = with(result) {
        when (game) {
            Game.WORLDLE,
            Game.TRADLE,
            Game.FLAGLE,
            Game.FRAMED -> 6

            Game.PINPOINT -> 5

            Game.TOP5,
            Game.GEOCIRCLES -> 10

            Game.TRAVLE -> (result.info<TravleInfo>()).maxPoints(score)

            Game.GEOGRID -> 9

            Game.BANDLE -> TODO()
        }
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
}
