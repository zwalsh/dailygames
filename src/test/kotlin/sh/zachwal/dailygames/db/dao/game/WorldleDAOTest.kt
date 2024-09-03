package sh.zachwal.dailygames.db.dao.game

import com.google.common.collect.Range
import com.google.common.truth.Truth.assertThat
import org.jdbi.v3.core.Jdbi
import org.jdbi.v3.core.statement.UnableToExecuteStatementException
import org.jdbi.v3.sqlobject.kotlin.onDemand
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import sh.zachwal.dailygames.db.extension.DatabaseExtension
import sh.zachwal.dailygames.db.extension.Fixtures
import sh.zachwal.dailygames.db.jdbi.puzzle.Game
import sh.zachwal.dailygames.db.jdbi.puzzle.Puzzle
import sh.zachwal.dailygames.db.jdbi.puzzle.WorldleResult
import java.time.Instant
import java.time.LocalDate
import kotlin.streams.toList

@ExtendWith(DatabaseExtension::class)
class WorldleDAOTest(
    jdbi: Jdbi,
    private val fixtures: Fixtures
) {
    private val puzzleDAO: PuzzleDAO = jdbi.onDemand()

    private val puzzleOne = Puzzle(Game.WORLDLE, 933, LocalDate.of(2024, 8, 11))
    private val puzzleTwo = Puzzle(Game.WORLDLE, 934, LocalDate.of(2024, 8, 12))

    private val worldleDAO: WorldleDAO = jdbi.onDemand()

    @BeforeEach
    fun addFixtures() {
        puzzleDAO.insertPuzzle(puzzleOne)
        puzzleDAO.insertPuzzle(puzzleTwo)
    }

    @Test
    fun `can insert a result`() {
        val shareText = """
            #Worldle #934 (12.08.2024) 4/6 (100%)
            🟩🟩🟩🟩🟨⬅️
            🟩🟩🟩🟩🟨⬅️
            🟩🟩🟩🟩🟨↗️
            🟩🟩🟩🟩🟩🎉
        """.trimIndent()

        val result = worldleDAO.insertResult(
            userId = fixtures.zach.id,
            puzzle = puzzleOne,
            score = 5,
            shareText = shareText.trimIndent(),
            scorePercentage = 100
        )

        assertThat(result.userId).isEqualTo(fixtures.zach.id)
        assertThat(result.game).isEqualTo(Game.WORLDLE)
        assertThat(result.puzzleNumber).isEqualTo(933)
        assertThat(result.puzzleDate).isEqualTo(LocalDate.of(2024, 8, 11))
        assertThat(result.instantSubmitted).isIn(Range.closed(Instant.now().minusSeconds(10), Instant.now()))
        assertThat(result.score).isEqualTo(5)
        assertThat(result.shareText).isEqualTo(shareText)
        assertThat(result.scorePercentage).isEqualTo(100)
    }

    @Test
    fun `does not allow score percentages below 0 or above 100`() {
        assertThrows<UnableToExecuteStatementException> {
            insertResult(scorePercentage = -1)
        }

        assertThrows<UnableToExecuteStatementException> {
            insertResult(scorePercentage = 101)
        }
    }

    @Test
    fun `can retrieve worldle result for user on a date`() {
        insertResult()

        val result = worldleDAO.resultForUserOnDate(fixtures.zach.id, LocalDate.of(2024, 8, 11))

        assertThat(result).isNotNull()
        assertThat(result!!.userId).isEqualTo(fixtures.zach.id)
        assertThat(result.game).isEqualTo(Game.WORLDLE)
        assertThat(result.puzzleNumber).isEqualTo(933)
        assertThat(result.puzzleDate).isEqualTo(LocalDate.of(2024, 8, 11))
        assertThat(result.instantSubmitted).isIn(Range.closed(Instant.now().minusSeconds(10), Instant.now()))
        assertThat(result.score).isEqualTo(5)
        assertThat(result.shareText).isEqualTo("")
        assertThat(result.scorePercentage).isEqualTo(100)
    }

    @Test
    fun `only retrieves results for given user`() {
        insertResult()
        insertResult(userId = fixtures.jackie.id)

        val jackieResult = worldleDAO.resultForUserOnDate(fixtures.jackie.id, puzzleOne.date!!)

        assertThat(jackieResult).isNotNull()
        assertThat(jackieResult!!.userId).isEqualTo(fixtures.jackie.id)

        val zachResult = worldleDAO.resultForUserOnDate(fixtures.zach.id, puzzleOne.date!!)

        assertThat(zachResult).isNotNull()
        assertThat(zachResult!!.userId).isEqualTo(fixtures.zach.id)
    }

    @Test
    fun `returns null when no result for user on date`() {
        val result = worldleDAO.resultForUserOnDate(fixtures.zach.id, LocalDate.of(2024, 8, 11))

        assertThat(result).isNull()
    }

    @Test
    fun `can list all results for a puzzle`() {
        insertResult()
        insertResult(userId = fixtures.jackie.id)
        insertResult(puzzle = puzzleTwo)

        val results = worldleDAO.resultsForPuzzle(puzzleOne).toList()

        assertThat(results).hasSize(2)
    }

    @Test
    fun `can list all results for all puzzles`() {
        insertResult()
        insertResult(userId = fixtures.jackie.id)
        insertResult(puzzle = puzzleTwo)

        val results = worldleDAO.allResultsStream().toList()

        assertThat(results).hasSize(3)
    }

    @Test
    fun `list all is ordered by submission time descending`() {
        val resultOne = insertResult()
        val resultTwo = insertResult(puzzle = puzzleTwo)
        val resultThree = insertResult(puzzle = puzzleTwo)

        val results = worldleDAO.allResultsStream().toList()

        assertThat(results).containsExactly(resultThree, resultTwo, resultOne)
    }

    @Test
    fun `can query by time range for a single user`() {
        val resultOne = insertResult()
        val resultOtherUser = insertResult(userId = fixtures.jackie.id)
        val resultTwo = insertResult(puzzle = puzzleTwo)
        Thread.sleep(1)
        val resultThree = insertResult(puzzle = puzzleTwo)

        val results = worldleDAO.resultsForUserInTimeRange(
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
        scorePercentage: Int = 100
    ): WorldleResult {
        return worldleDAO.insertResult(userId, puzzle, score, shareText, scorePercentage)
    }
}
