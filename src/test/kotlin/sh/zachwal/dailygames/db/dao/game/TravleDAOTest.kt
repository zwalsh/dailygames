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
import sh.zachwal.dailygames.db.jdbi.puzzle.Puzzle
import sh.zachwal.dailygames.db.jdbi.puzzle.TravleResult
import sh.zachwal.dailygames.results.TRAVLE_PLUS_0
import java.time.Instant
import kotlin.streams.toList

@ExtendWith(DatabaseExtension::class)
class TravleDAOTest(
    jdbi: Jdbi,
    private val fixtures: Fixtures
) {
    private val puzzleDAO: PuzzleDAO = jdbi.onDemand()

    private val puzzleOne = Puzzle(Game.TRAVLE, 607, null)
    private val puzzleTwo = Puzzle(Game.TRAVLE, 608, null)

    private val travleDAO: TravleDAO = jdbi.onDemand()

    @BeforeEach
    fun addFixtures() {
        puzzleDAO.insertPuzzle(puzzleOne)
        puzzleDAO.insertPuzzle(puzzleTwo)
    }

    @Test
    fun `can insert a result`() {
        val result = travleDAO.insertResult(
            userId = fixtures.zach.id,
            puzzle = puzzleOne,
            score = 0,
            shareText = TRAVLE_PLUS_0,
            numGuesses = 7,
            numIncorrect = 0,
            numPerfect = 6,
            numHints = 0,
        )

        assertThat(result.userId).isEqualTo(fixtures.zach.id)
        assertThat(result.game).isEqualTo(Game.TRAVLE)
        assertThat(result.puzzleNumber).isEqualTo(puzzleOne.number)
        assertThat(result.puzzleDate).isEqualTo(null)
        assertThat(result.instantSubmitted).isIn(Range.closed(Instant.now().minusSeconds(10), Instant.now()))
        assertThat(result.score).isEqualTo(0)
        assertThat(result.shareText).isEqualTo(TRAVLE_PLUS_0)
        assertThat(result.numGuesses).isEqualTo(7)
        assertThat(result.numIncorrect).isEqualTo(0)
        assertThat(result.numPerfect).isEqualTo(6)
        assertThat(result.numHints).isEqualTo(0)
    }

    @Test
    fun `can retrieve Travle result for user for a puzzle`() {
        insertResult(puzzle = puzzleOne)

        val result = travleDAO.resultForUserOnPuzzle(fixtures.zach.id, puzzleOne)

        assertThat(result).isNotNull()
        assertThat(result!!.userId).isEqualTo(fixtures.zach.id)
        assertThat(result.game).isEqualTo(Game.TRAVLE)
        assertThat(result.puzzleNumber).isEqualTo(puzzleOne.number)
        assertThat(result.puzzleDate).isEqualTo(null)
        assertThat(result.instantSubmitted).isIn(Range.closed(Instant.now().minusSeconds(10), Instant.now()))
        assertThat(result.score).isEqualTo(5)
        assertThat(result.shareText).isEqualTo("")
    }

    @Test
    fun `only retrieves results for given user`() {
        insertResult()
        insertResult(userId = fixtures.jackie.id)

        val jackieResult = travleDAO.resultForUserOnPuzzle(fixtures.jackie.id, puzzleOne)

        assertThat(jackieResult).isNotNull()
        assertThat(jackieResult!!.userId).isEqualTo(fixtures.jackie.id)

        val zachResult = travleDAO.resultForUserOnPuzzle(fixtures.zach.id, puzzleOne)

        assertThat(zachResult).isNotNull()
        assertThat(zachResult!!.userId).isEqualTo(fixtures.zach.id)
    }

    @Test
    fun `returns null when no result for user on date`() {
        val result = travleDAO.resultForUserOnPuzzle(fixtures.jackie.id, puzzleOne)

        assertThat(result).isNull()
    }

    @Test
    fun `can list all results for a puzzle`() {
        insertResult()
        insertResult(userId = fixtures.jackie.id)
        insertResult(puzzle = puzzleTwo)

        val results = travleDAO.resultsForPuzzle(puzzleOne)

        assertThat(results).hasSize(2)
    }

    @Test
    fun `can list all results for all puzzles`() {
        insertResult()
        insertResult(userId = fixtures.jackie.id)
        insertResult(puzzle = puzzleTwo)

        val results = travleDAO.allResultsStream().toList()

        assertThat(results).hasSize(3)
    }

    @Test
    fun `list all is ordered by puzzle_number descending, then submission time descending`() {
        val resultOne = insertResult()
        val resultTwo = insertResult(puzzle = puzzleTwo)
        val resultThree = insertResult(puzzle = puzzleTwo)

        val results = travleDAO.allResultsStream().toList()

        assertThat(results).containsExactly(resultThree, resultTwo, resultOne)
    }


    @Test
    fun `can query by time range for a single user`() {
        val resultOne = insertResult()
        val resultOtherUser = insertResult(userId = fixtures.jackie.id)
        val resultTwo = insertResult(puzzle = puzzleTwo)
        Thread.sleep(1)
        val resultThree = insertResult(puzzle = puzzleTwo)

        val results = travleDAO.resultsForUserInTimeRange(
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
        puzzle: Puzzle = puzzleOne
    ): TravleResult {
        return travleDAO.insertResult(
            userId = userId,
            puzzle = puzzle,
            score = 5,
            shareText = "",
            numGuesses = 7,
            numIncorrect = 0,
            numPerfect = 6,
            numHints = 0,
        )
    }
}
