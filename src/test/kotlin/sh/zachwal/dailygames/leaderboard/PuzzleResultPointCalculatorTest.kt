package sh.zachwal.dailygames.leaderboard

import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.Test
import sh.zachwal.dailygames.db.jdbi.puzzle.FlagleResult
import sh.zachwal.dailygames.db.jdbi.puzzle.Game
import sh.zachwal.dailygames.db.jdbi.puzzle.PinpointResult
import sh.zachwal.dailygames.db.jdbi.puzzle.Top5Result
import sh.zachwal.dailygames.db.jdbi.puzzle.TradleResult
import sh.zachwal.dailygames.db.jdbi.puzzle.TravleResult
import sh.zachwal.dailygames.db.jdbi.puzzle.WorldleResult
import java.time.Instant

class PuzzleResultPointCalculatorTest {
    private val calculator = PuzzleResultPointCalculator()

    @Test
    fun `returns 7 minus score for worldle`() {
        val result = WorldleResult(
            id = 1L,
            userId = 1L,
            game = Game.WORLDLE,
            score = 5,
            puzzleNumber = 30,
            puzzleDate = null,
            instantSubmitted = Instant.now(),
            shareText = "",
            scorePercentage = 100,
        )
        assertThat(calculator.calculatePoints(result)).isEqualTo(2)
    }

    @Test
    fun `returns 7 minus score for flagle`() {
        val result = FlagleResult(
            id = 1L,
            userId = 1L,
            game = Game.FLAGLE,
            score = 6,
            puzzleNumber = 30,
            puzzleDate = null,
            instantSubmitted = Instant.now(),
            shareText = "",
        )
        assertThat(calculator.calculatePoints(result)).isEqualTo(1)
    }

    @Test
    fun `returns 7 minus score for tradle`() {
        val result = TradleResult(
            id = 1L,
            userId = 1L,
            game = Game.TRADLE,
            score = 7,
            puzzleNumber = 30,
            puzzleDate = null,
            instantSubmitted = Instant.now(),
            shareText = "",
        )
        assertThat(calculator.calculatePoints(result)).isEqualTo(0)
    }

    @Test
    fun `returns score directly for top5`() {
        val result = Top5Result(
            id = 1L,
            userId = 1L,
            game = Game.TOP5,
            score = 5,
            puzzleNumber = 30,
            puzzleDate = null,
            instantSubmitted = Instant.now(),
            shareText = "",
            numGuesses = 0,
            numCorrect = 0,
            isPerfect = false,
        )
        assertThat(calculator.calculatePoints(result)).isEqualTo(5)
    }

    private val travleResult = TravleResult(
        id = 1L,
        userId = 1L,
        game = Game.TRAVLE,
        score = 0,
        puzzleNumber = 30,
        puzzleDate = null,
        instantSubmitted = Instant.now(),
        shareText = "",
        numGuesses = 7,
        numIncorrect = 0,
        numPerfect = 7,
        numHints = 0,
    )

    @Test
    fun `returns number of allowed incorrect guesses plus one minus actual guesses for travle`() {
        // When the Travle puzzle has a shortest solution of 7 guesses
        assertThat(travleResult.numGuesses).isEqualTo(7)
        assertThat(travleResult.score).isEqualTo(0)

        // Then the number of allowed incorrect guesses is 6
        // And the number of incorrect guesses in this case is 0
        // So the score is 7

        assertThat(calculator.calculatePoints(travleResult)).isEqualTo(7)
    }

    @Test
    fun `returns zero when travle score is negative`() {
        val result = travleResult.copy(score = -1)
        assertThat(calculator.calculatePoints(result)).isEqualTo(0)
    }

    @Test
    fun `deducts one point for each incorrect guess in travle`() {
        for (numWrong in 1..7) {
            // shortest solution is 7
            val result = travleResult.copy(score = numWrong, numGuesses = 7 + numWrong, numIncorrect = numWrong)
            assertThat(calculator.calculatePoints(result)).isEqualTo(7 - numWrong)
        }
    }

    @Test
    fun `allowed guesses in travle matches the given table`() {
        /**
         * From the Travle website:
         *
         * # Guesses in Shortest Solution	# Extra Guesses
         * 3	4
         * 4-6	5
         * 7-9	6
         * 10-12	7
         * 13+	8
         *
         */
        val allowedGuesses = mapOf(
            3 to 4,
            4 to 5,
            5 to 5,
            6 to 5,
            7 to 6,
            8 to 6,
            9 to 6,
            10 to 7,
            11 to 7,
            12 to 7,
            13 to 8,
            14 to 8,
            15 to 8
        )

        allowedGuesses.entries.forEach { (shortestSolution, allowedIncorrect) ->
            // perfect result at this solution length
            val result = travleResult.copy(numGuesses = shortestSolution, score = 0)
            assertThat(calculator.calculatePoints(result)).isEqualTo(allowedIncorrect + 1)
        }
    }

    @Test
    fun `returns 6 minus score for pinpoint`() {
        val result = PinpointResult(
            id = 1L,
            userId = 1L,
            game = Game.PINPOINT,
            score = 2,
            puzzleNumber = 30,
            puzzleDate = null,
            instantSubmitted = Instant.now(),
            shareText = "",
        )
        assertThat(calculator.calculatePoints(result)).isEqualTo(4)
    }
}
