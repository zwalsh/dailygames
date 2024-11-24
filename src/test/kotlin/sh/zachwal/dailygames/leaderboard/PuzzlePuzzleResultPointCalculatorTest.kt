package sh.zachwal.dailygames.leaderboard

import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.Test
import sh.zachwal.dailygames.db.jdbi.puzzle.Game
import sh.zachwal.dailygames.db.jdbi.puzzle.PuzzleResult
import sh.zachwal.dailygames.results.resultinfo.FlagleInfo
import sh.zachwal.dailygames.results.resultinfo.GeocirclesInfo
import sh.zachwal.dailygames.results.resultinfo.PinpointInfo
import sh.zachwal.dailygames.results.resultinfo.Top5Info
import sh.zachwal.dailygames.results.resultinfo.TradleInfo
import sh.zachwal.dailygames.results.resultinfo.TravleInfo
import sh.zachwal.dailygames.results.resultinfo.WorldleInfo
import java.time.Instant

class PuzzlePuzzleResultPointCalculatorTest {
    private val calculator = PuzzleResultPointCalculator()

    private val worldleResult = PuzzleResult(
        id = 1L,
        userId = 1L,
        game = Game.WORLDLE,
        puzzleNumber = 30,
        instantSubmitted = Instant.now(),
        puzzleDate = null,
        score = 5,
        shareText = "",
        resultInfo = WorldleInfo(
            percentage = 100
        ),
    )

    @Test
    fun `returns 7 minus score for worldle result`() {
        assertThat(calculator.calculatePoints(worldleResult)).isEqualTo(2)
    }

    private val flagleResult = PuzzleResult(
        id = 1L,
        userId = 1L,
        game = Game.FLAGLE,
        puzzleNumber = 30,
        instantSubmitted = Instant.now(),
        puzzleDate = null,
        score = 6,
        shareText = "",
        resultInfo = FlagleInfo
    )

    @Test
    fun `returns 7 minus score for flagle result`() {
        assertThat(calculator.calculatePoints(flagleResult)).isEqualTo(1)
    }

    private val tradleResult = PuzzleResult(
        id = 1L,
        userId = 1L,
        game = Game.TRADLE,
        puzzleNumber = 30,
        instantSubmitted = Instant.now(),
        puzzleDate = null,
        score = 7,
        shareText = "",
        resultInfo = TradleInfo,
    )

    @Test
    fun `returns 7 minus score for tradle result`() {
        assertThat(calculator.calculatePoints(tradleResult)).isEqualTo(0)
    }

    private val top5Result = PuzzleResult(
        id = 1L,
        userId = 1L,
        game = Game.TOP5,
        puzzleNumber = 30,
        instantSubmitted = Instant.now(),
        puzzleDate = null,
        score = 5,
        shareText = "",
        resultInfo = Top5Info(
            numGuesses = 0,
            numCorrect = 0,
            isPerfect = false,
        ),
    )

    @Test
    fun `returns score directly for top5 result`() {
        assertThat(calculator.calculatePoints(top5Result)).isEqualTo(5)
    }

    private val travleResult = PuzzleResult(
        id = 1L,
        userId = 1L,
        game = Game.TRAVLE,
        puzzleNumber = 30,
        instantSubmitted = Instant.now(),
        puzzleDate = null,
        score = 0,
        shareText = "",
        resultInfo = TravleInfo(
            numGuesses = 7,
            numIncorrect = 0,
            numPerfect = 7,
            numHints = 0,
        ),
    )

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
    fun `returns number of allowed incorrect guesses plus one minus actual guesses for travle`() {
        // When the Travle puzzle has a shortest solution of 7 guesses
        val info = travleResult.info<TravleInfo>()
        assertThat(info.numGuesses).isEqualTo(7)
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
            val result = travleResult.copy(
                score = numWrong,
                resultInfo = TravleInfo(
                    numGuesses = 7 + numWrong,
                    numIncorrect = numWrong,
                    numPerfect = 7,
                    numHints = 0,
                ),
            )
            assertThat(calculator.calculatePoints(result)).isEqualTo(7 - numWrong)
        }
    }

    @Test
    fun `allowed guesses in travle matches the given table for travle`() {
        travleExtraGuessesPerShortestSolution.entries.forEach { (shortestSolution, allowedIncorrect) ->
            // perfect result at this solution length
            val info = travleResult.info<TravleInfo>()
            val result = travleResult.copy(
                score = 0,
                resultInfo = info.copy(
                    numGuesses = shortestSolution,
                ),
            )
            assertThat(calculator.calculatePoints(result)).isEqualTo(allowedIncorrect + 1)
        }
    }

    private val pinpointResult = PuzzleResult(
        id = 1L,
        userId = 1L,
        game = Game.PINPOINT,
        puzzleNumber = 30,
        instantSubmitted = Instant.now(),
        puzzleDate = null,
        score = 2,
        shareText = "",
        resultInfo = PinpointInfo,
    )

    @Test
    fun `returns 6 minus score for pinpoint result`() {
        assertThat(calculator.calculatePoints(pinpointResult)).isEqualTo(4)
    }

    private val geocirclesResult = PuzzleResult(
        id = 1L,
        userId = 1L,
        game = Game.GEOCIRCLES,
        puzzleNumber = 30,
        instantSubmitted = Instant.now(),
        puzzleDate = null,
        score = 8,
        shareText = "",
        resultInfo = GeocirclesInfo,
    )

    @Test
    fun `returns score directly for geocircles`() {
        assertThat(calculator.calculatePoints(geocirclesResult)).isEqualTo(8)
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
            val result = travleResult.copy(
                score = 0,
                resultInfo = (travleResult.info<TravleInfo>()).copy(
                    numGuesses = shortestSolution
                )
            )
            assertThat(calculator.maxPoints(result)).isEqualTo(allowedIncorrect + 1)
        }
    }

    @Test
    fun `max points for travle matches given table if score is negative`() {
        travleExtraGuessesPerShortestSolution.entries.forEach { (shortestSolution, allowedIncorrect) ->
            val result = travleResult.copy(
                score = -allowedIncorrect,
                resultInfo = (travleResult.info<TravleInfo>()).copy(
                    numGuesses = shortestSolution + allowedIncorrect
                )
            )
            assertThat(calculator.maxPoints(result)).isEqualTo(allowedIncorrect + 1)
        }
    }

    @Test
    fun `max points for travle matches given table if player was arbitrarily far away from the destination country`() {
        travleExtraGuessesPerShortestSolution.entries.forEach { (shortestSolution, allowedIncorrect) ->
            val result = travleResult.copy(
                score = -10,
                resultInfo = (travleResult.info<TravleInfo>()).copy(
                    numGuesses = shortestSolution + allowedIncorrect
                )
            )
            assertThat(calculator.maxPoints(result)).isEqualTo(allowedIncorrect + 1)
        }
    }

    @Test
    fun `max points for travle matches on travle 669`() {
        /**
         * #travle #669 (2 away)
         * 🟧🟩🟥🟧🟧🟩🟧🟧🟩🟧
         */
        val resultTwoAway = travleResult.copy(
            score = -2,
            resultInfo = (travleResult.info<TravleInfo>()).copy(
                numGuesses = 10,
                numIncorrect = 7,
                numPerfect = 0,
            )
        )

        /**
         * #travle #669 +0
         * ✅🟩✅✅✅
         */
        val resultPlusZero = travleResult.copy(
            score = 0,
            resultInfo = (travleResult.info<TravleInfo>()).copy(
                numGuesses = 5,
                numIncorrect = 0,
                numPerfect = 4,
            )
        )

        assertThat(calculator.maxPoints(resultTwoAway))
            .isEqualTo(calculator.maxPoints(resultPlusZero))
    }

    @Test
    fun `max points for travle matches given table if score is positive`() {
        travleExtraGuessesPerShortestSolution.entries.forEach { (shortestSolution, allowedIncorrect) ->
            val result = travleResult.copy(
                score = 1,
                resultInfo = (travleResult.info<TravleInfo>()).copy(
                    numGuesses = shortestSolution + 1
                )
            )
            assertThat(calculator.maxPoints(result)).isEqualTo(allowedIncorrect + 1)
        }
    }
}
