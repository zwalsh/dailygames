package sh.zachwal.dailygames.db.dao.game

import com.google.common.collect.Range
import com.google.common.truth.Truth.assertThat
import org.jdbi.v3.core.Jdbi
import org.jdbi.v3.sqlobject.kotlin.onDemand
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import sh.zachwal.dailygames.db.extension.DatabaseExtension
import sh.zachwal.dailygames.db.extension.Fixtures
import sh.zachwal.dailygames.db.jdbi.puzzle.Game
import sh.zachwal.dailygames.db.jdbi.puzzle.PinpointResult
import sh.zachwal.dailygames.db.jdbi.puzzle.Puzzle
import java.time.Instant
import kotlin.streams.toList

@ExtendWith(DatabaseExtension::class)
class PinpointDAOTest(
    jdbi: Jdbi,
    private val fixtures: Fixtures
) {
    private val puzzleDAO: PuzzleDAO = jdbi.onDemand()

    private val puzzleOne = Puzzle(Game.PINPOINT, 123, null)
    private val puzzleTwo = Puzzle(Game.FLAGLE, 124, null)

    private val pinpointDAO: PinpointDAO = jdbi.onDemand()

    @BeforeEach
    fun addFixtures() {
        puzzleDAO.insertPuzzle(puzzleOne)
        puzzleDAO.insertPuzzle(puzzleTwo)
    }

    @Test
    fun `can insert a result`() {
        val result = pinpointDAO.insertResult(
            userId = fixtures.zach.id,
            puzzle = puzzleOne,
            score = 3,
            shareText = "",
        )

        assertThat(result.userId).isEqualTo(fixtures.zach.id)
        assertThat(result.game).isEqualTo(Game.PINPOINT)
        assertThat(result.puzzleNumber).isEqualTo(123)
        assertThat(result.instantSubmitted).isIn(Range.closed(Instant.now().minusSeconds(10), Instant.now()))
        assertThat(result.score).isEqualTo(3)
        assertThat(result.shareText).isEqualTo("")
    }

    @Test
    fun `can retrieve pinpoint result for user and puzzle`() {
        insertResult(puzzle = puzzleOne)

        val result = pinpointDAO.resultForUserOnPuzzle(fixtures.zach.id, puzzleOne)

        assertThat(result).isNotNull()
        assertThat(result!!.userId).isEqualTo(fixtures.zach.id)
        assertThat(result.game).isEqualTo(Game.PINPOINT)
        assertThat(result.puzzleNumber).isEqualTo(123)
        assertThat(result.instantSubmitted).isIn(Range.closed(Instant.now().minusSeconds(10), Instant.now()))
        assertThat(result.score).isEqualTo(5)
        assertThat(result.shareText).isEqualTo("")
    }

    @Test
    fun `only retrieves results for given user`() {
        insertResult()
        insertResult(userId = fixtures.jackie.id)

        val jackieResult = pinpointDAO.resultForUserOnPuzzle(fixtures.jackie.id, puzzleOne)

        assertThat(jackieResult).isNotNull()
        assertThat(jackieResult!!.userId).isEqualTo(fixtures.jackie.id)

        val zachResult = pinpointDAO.resultForUserOnPuzzle(fixtures.zach.id, puzzleOne)

        assertThat(zachResult).isNotNull()
        assertThat(zachResult!!.userId).isEqualTo(fixtures.zach.id)
    }

    @Test
    fun `returns null when no result for user on date`() {
        val result = pinpointDAO.resultForUserOnPuzzle(fixtures.zach.id, puzzleOne)

        assertThat(result).isNull()
    }

    @Test
    fun `can list all results for a puzzle`() {
        insertResult()
        insertResult(userId = fixtures.jackie.id)
        insertResult(puzzle = puzzleTwo)

        val results = pinpointDAO.resultsForPuzzle(puzzleOne).toList()

        assertThat(results).hasSize(2)
    }

    @Test
    fun `can list all results for all puzzles`() {
        insertResult()
        insertResult(userId = fixtures.jackie.id)
        insertResult(puzzle = puzzleTwo)

        val results = pinpointDAO.allResultsStream().toList()

        assertThat(results).hasSize(3)
    }

    @Test
    fun `list all is ordered by submission time descending`() {
        val resultOne = insertResult()
        val resultTwo = insertResult(puzzle = puzzleTwo)
        val resultThree = insertResult(puzzle = puzzleTwo)

        val results = pinpointDAO.allResultsStream().toList()

        assertThat(results).containsExactly(resultThree, resultTwo, resultOne)
    }

    @Test
    fun `can query by time range for a single user`() {
        val resultOne = insertResult()
        val resultOtherUser = insertResult(userId = fixtures.jackie.id)
        val resultTwo = insertResult(puzzle = puzzleTwo)
        Thread.sleep(1)
        val resultThree = insertResult(puzzle = puzzleTwo)

        val results = pinpointDAO.resultsForUserInTimeRange(
            userId = fixtures.zach.id,
            start = resultOne.instantSubmitted,
            end = resultTwo.instantSubmitted,
        )

        assertThat(results).containsExactly(resultOne, resultTwo)
        assertThat(results).doesNotContain(resultThree)
        assertThat(results).doesNotContain(resultOtherUser)
    }

    private fun insertResult(
        userId: Long = fixtures.zach.id,
        puzzle: Puzzle = puzzleOne,
        score: Int = 5,
        shareText: String = "",
    ): PinpointResult {
        return pinpointDAO.insertResult(userId, puzzle, score, shareText)
    }
}
