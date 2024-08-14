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
import sh.zachwal.dailygames.db.jdbi.puzzle.FlagleResult
import sh.zachwal.dailygames.db.jdbi.puzzle.Game
import sh.zachwal.dailygames.db.jdbi.puzzle.Puzzle
import sh.zachwal.dailygames.results.FLAGLE
import java.time.Instant
import java.time.LocalDate
import kotlin.streams.toList

@ExtendWith(DatabaseExtension::class)
class FlagleDAOTest(
    jdbi: Jdbi,
    private val fixtures: Fixtures
) {
    private val puzzleDAO: PuzzleDAO = jdbi.onDemand()

    private val puzzleOne = Puzzle(Game.FLAGLE, 933, LocalDate.of(2024, 8, 11))
    private val puzzleTwo = Puzzle(Game.FLAGLE, 934, LocalDate.of(2024, 8, 12))

    private val flagleDAO: FlagleDAO = jdbi.onDemand()

    @BeforeEach
    fun addFixtures() {
        puzzleDAO.insertPuzzle(puzzleOne)
        puzzleDAO.insertPuzzle(puzzleTwo)
    }

    @Test
    fun `can insert a result`() {
        val result = flagleDAO.insertResult(
            userId = fixtures.zach.id,
            puzzle = puzzleOne,
            score = 5,
            shareText = FLAGLE,
        )

        assertThat(result.userId).isEqualTo(fixtures.zach.id)
        assertThat(result.game).isEqualTo(Game.FLAGLE)
        assertThat(result.puzzleNumber).isEqualTo(933)
        assertThat(result.puzzleDate).isEqualTo(LocalDate.of(2024, 8, 11))
        assertThat(result.instantSubmitted).isIn(Range.closed(Instant.now().minusSeconds(10), Instant.now()))
        assertThat(result.score).isEqualTo(5)
        assertThat(result.shareText).isEqualTo(FLAGLE)
    }

    @Test
    fun `can retrieve flagle result for user on a date`() {
        insertResult()

        val result = flagleDAO.resultForUserOnDate(fixtures.zach.id, LocalDate.of(2024, 8, 11))

        assertThat(result).isNotNull()
        assertThat(result!!.userId).isEqualTo(fixtures.zach.id)
        assertThat(result.game).isEqualTo(Game.FLAGLE)
        assertThat(result.puzzleNumber).isEqualTo(933)
        assertThat(result.puzzleDate).isEqualTo(LocalDate.of(2024, 8, 11))
        assertThat(result.instantSubmitted).isIn(Range.closed(Instant.now().minusSeconds(10), Instant.now()))
        assertThat(result.score).isEqualTo(5)
        assertThat(result.shareText).isEqualTo("")
    }

    @Test
    fun `only retrieves results for given user`() {
        insertResult()
        insertResult(userId = fixtures.jackie.id)

        val jackieResult = flagleDAO.resultForUserOnDate(fixtures.jackie.id, puzzleOne.date!!)

        assertThat(jackieResult).isNotNull()
        assertThat(jackieResult!!.userId).isEqualTo(fixtures.jackie.id)

        val zachResult = flagleDAO.resultForUserOnDate(fixtures.zach.id, puzzleOne.date!!)

        assertThat(zachResult).isNotNull()
        assertThat(zachResult!!.userId).isEqualTo(fixtures.zach.id)
    }

    @Test
    fun `returns null when no result for user on date`() {
        val result = flagleDAO.resultForUserOnDate(fixtures.zach.id, LocalDate.of(2024, 8, 11))

        assertThat(result).isNull()
    }

    @Test
    fun `can list all results for a puzzle`() {
        insertResult()
        insertResult(userId = fixtures.jackie.id)
        insertResult(puzzle = puzzleTwo)

        val results = flagleDAO.resultsForPuzzle(puzzleOne).toList()

        assertThat(results).hasSize(2)
    }

    @Test
    fun `can list all results for all puzzles`() {
        insertResult()
        insertResult(userId = fixtures.jackie.id)
        insertResult(puzzle = puzzleTwo)

        val results = flagleDAO.allResultsStream().toList()

        assertThat(results).hasSize(3)
    }

    @Test
    fun `list all is ordered by submission time descending`() {
        val resultOne = insertResult()
        val resultTwo = insertResult(puzzle = puzzleTwo)
        val resultThree = insertResult(puzzle = puzzleTwo)

        val results = flagleDAO.allResultsStream().toList()

        assertThat(results).containsExactly(resultThree, resultTwo, resultOne)
    }

    private fun insertResult(
        userId: Long = fixtures.zach.id,
        puzzle: Puzzle = puzzleOne,
        score: Int = 5,
        shareText: String = "",
    ): FlagleResult {
        return flagleDAO.insertResult(userId, puzzle, score, shareText)
    }
}
