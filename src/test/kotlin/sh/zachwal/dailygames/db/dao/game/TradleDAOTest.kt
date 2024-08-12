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
import sh.zachwal.dailygames.db.jdbi.puzzle.TradleResult
import java.time.Instant
import java.time.LocalDate
import kotlin.streams.toList

@ExtendWith(DatabaseExtension::class)
class TradleDAOTest(
    jdbi: Jdbi,
    private val fixtures: Fixtures
) {
    private val puzzleDAO: PuzzleDAO = jdbi.onDemand()

    private val puzzleOne = Puzzle(Game.TRADLE, 543, null)
    private val puzzleTwo = Puzzle(Game.TRADLE, 544, null)

    private val tradleDAO: TradleDAO = jdbi.onDemand()

    @BeforeEach
    fun addFixtures() {
        puzzleDAO.insertPuzzle(puzzleOne)
        puzzleDAO.insertPuzzle(puzzleTwo)
    }

    @Test
    fun `can insert a result`() {
        val shareText = """
            #Tradle #934 4/6
            🟩🟩🟩🟩🟨⬅️
            🟩🟩🟩🟩🟨⬅️
            🟩🟩🟩🟩🟨↗️
            🟩🟩🟩🟩🟩🎉
        """.trimIndent()

        val result = tradleDAO.insertResult(
            userId = fixtures.zach.id,
            puzzle = puzzleOne,
            score = 5,
            shareText = shareText.trimIndent(),
        )

        assertThat(result.userId).isEqualTo(fixtures.zach.id)
        assertThat(result.game).isEqualTo(Game.TRADLE)
        assertThat(result.puzzleNumber).isEqualTo(puzzleOne.number)
        assertThat(result.puzzleDate).isNull()
        assertThat(result.instantSubmitted).isIn(Range.closed(Instant.now().minusSeconds(10), Instant.now()))
        assertThat(result.score).isEqualTo(5)
        assertThat(result.shareText).isEqualTo(shareText)
    }

    @Test
    fun `can retrieve Tradle result for user for a puzzle`() {
        insertResult(puzzle = puzzleOne)

        val result = tradleDAO.resultForUserOnPuzzle(fixtures.zach.id, puzzleOne)

        assertThat(result).isNotNull()
        assertThat(result!!.userId).isEqualTo(fixtures.zach.id)
        assertThat(result.game).isEqualTo(Game.TRADLE)
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

        val jackieResult = tradleDAO.resultForUserOnPuzzle(fixtures.jackie.id, puzzleOne)

        assertThat(jackieResult).isNotNull()
        assertThat(jackieResult!!.userId).isEqualTo(fixtures.jackie.id)

        val zachResult = tradleDAO.resultForUserOnPuzzle(fixtures.zach.id, puzzleOne)

        assertThat(zachResult).isNotNull()
        assertThat(zachResult!!.userId).isEqualTo(fixtures.zach.id)
    }

    @Test
    fun `returns null when no result for user on date`() {
        val result = tradleDAO.resultForUserOnPuzzle(fixtures.jackie.id, puzzleOne)

        assertThat(result).isNull()
    }

    @Test
    fun `can list all results for a puzzle`() {
        insertResult()
        insertResult(userId = fixtures.jackie.id)
        insertResult(puzzle = puzzleTwo)

        val results = tradleDAO.resultsForPuzzleStream(puzzleOne).toList()

        assertThat(results).hasSize(2)
    }

    @Test
    fun `can list all results for all puzzles`() {
        insertResult()
        insertResult(userId = fixtures.jackie.id)
        insertResult(puzzle = puzzleTwo)

        val results = tradleDAO.allResultsStream().toList()

        assertThat(results).hasSize(3)
    }

    @Test
    fun `list all is ordered by puzzle_number descending, then submission time descending`() {
        val resultOne = insertResult()
        val resultTwo = insertResult(puzzle = puzzleTwo)
        val resultThree = insertResult(puzzle = puzzleTwo)

        val results = tradleDAO.allResultsStream().toList()

        assertThat(results).containsExactly(resultThree, resultTwo, resultOne)
    }

    private fun insertResult(
        userId: Long = fixtures.zach.id,
        puzzle: Puzzle = puzzleOne,
        score: Int = 5,
        shareText: String = ""
    ): TradleResult {
        return tradleDAO.insertResult(userId, puzzle, score, shareText)
    }
}
