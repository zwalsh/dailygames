package sh.zachwal.dailygames.leaderboard

import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.Test
import sh.zachwal.dailygames.db.jdbi.puzzle.FlagleResult
import sh.zachwal.dailygames.db.jdbi.puzzle.Game
import sh.zachwal.dailygames.db.jdbi.puzzle.GeocirclesResult
import sh.zachwal.dailygames.db.jdbi.puzzle.PinpointResult
import sh.zachwal.dailygames.db.jdbi.puzzle.Top5Result
import sh.zachwal.dailygames.db.jdbi.puzzle.TradleResult
import sh.zachwal.dailygames.db.jdbi.puzzle.TravleResult
import sh.zachwal.dailygames.db.jdbi.puzzle.WorldleResult
import java.time.Instant

class PuzzleResultPointCalculatorTest {
    private val calculator = PuzzleResultPointCalculator()

    val worldleResult = WorldleResult(
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
    @Test
    fun `returns 7 minus score for worldle`() {
        assertThat(calculator.calculatePoints(worldleResult)).isEqualTo(2)
    }

    val flagleResult = FlagleResult(
        id = 1L,
        userId = 1L,
        game = Game.FLAGLE,
        score = 6,
        puzzleNumber = 30,
        puzzleDate = null,
        instantSubmitted = Instant.now(),
        shareText = "",
    )
    @Test
    fun `returns 7 minus score for flagle`() {
        assertThat(calculator.calculatePoints(flagleResult)).isEqualTo(1)
    }

    val tradleResult = TradleResult(
        id = 1L,
        userId = 1L,
        game = Game.TRADLE,
        score = 7,
        puzzleNumber = 30,
        puzzleDate = null,
        instantSubmitted = Instant.now(),
        shareText = "",
    )
    @Test
    fun `returns 7 minus score for tradle`() {
        assertThat(calculator.calculatePoints(tradleResult)).isEqualTo(0)
    }

    val top5Result = Top5Result(
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
    @Test
    fun `returns score directly for top5`() {
        assertThat(calculator.calculatePoints(top5Result)).isEqualTo(5)
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
    private val travleExtraGuessesPerShortestSolution = mapOf(
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

    @Test
    fun `allowed guesses in travle matches the given table`() {
        travleExtraGuessesPerShortestSolution.entries.forEach { (shortestSolution, allowedIncorrect) ->
            // perfect result at this solution length
            val result = travleResult.copy(numGuesses = shortestSolution, score = 0)
            assertThat(calculator.calculatePoints(result)).isEqualTo(allowedIncorrect + 1)
        }
    }

    private val pinpointResult = PinpointResult(
        id = 1L,
        userId = 1L,
        game = Game.PINPOINT,
        score = 2,
        puzzleNumber = 30,
        puzzleDate = null,
        instantSubmitted = Instant.now(),
        shareText = "",
    )
    @Test
    fun `returns 6 minus score for pinpoint`() {
        assertThat(calculator.calculatePoints(pinpointResult)).isEqualTo(4)
    }

    private val geocirclesResult = GeocirclesResult(
        id = 1L,
        userId = 1L,
        game = Game.GEOCIRCLES,
        score = 5,
        puzzleNumber = 30,
        puzzleDate = null,
        instantSubmitted = Instant.now(),
        shareText = "",
    )
    @Test
    fun `returns score directly for geocircles`() {
        assertThat(calculator.calculatePoints(geocirclesResult)).isEqualTo(5)
    }

    @Test
    fun `returns max score for simple games`() {
        assertThat(calculator.maxPoints(worldleResult)).isEqualTo(6)
        assertThat(calculator.maxPoints(flagleResult)).isEqualTo(6)
        assertThat(calculator.maxPoints(tradleResult)).isEqualTo(6)
        assertThat(calculator.maxPoints(top5Result)).isEqualTo(10)
        assertThat(calculator.maxPoints(pinpointResult)).isEqualTo(5)
        assertThat(calculator.maxPoints(geocirclesResult)).isEqualTo(10)
    }

    @Test
    fun `max points for travle matches given table for perfect scores`() {
        travleExtraGuessesPerShortestSolution.entries.forEach { (shortestSolution, allowedIncorrect) ->
            // perfect result at this solution length
            val result = travleResult.copy(numGuesses = shortestSolution, score = 0)
            assertThat(calculator.maxPoints(result)).isEqualTo(allowedIncorrect + 1)
        }
    }

    @Test
    fun `max points for travle matches given table if score is negative`() {
        travleExtraGuessesPerShortestSolution.entries.forEach { (shortestSolution, allowedIncorrect) ->
            val result = travleResult.copy(numGuesses = shortestSolution + allowedIncorrect, score = -allowedIncorrect)
            assertThat(calculator.maxPoints(result)).isEqualTo(allowedIncorrect + 1)
        }
    }

    @Test
    fun `max points for travle matches given table if score is positive`() {
        travleExtraGuessesPerShortestSolution.entries.forEach { (shortestSolution, allowedIncorrect) ->
            val result = travleResult.copy(numGuesses = shortestSolution + 1, score = 1)
            assertThat(calculator.maxPoints(result)).isEqualTo(allowedIncorrect + 1)
        }
    }
}
