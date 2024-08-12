package sh.zachwal.dailygames.db.dao

import com.google.common.collect.Range
import com.google.common.truth.Truth.assertThat
import org.jdbi.v3.core.Jdbi
import org.jdbi.v3.core.statement.UnableToExecuteStatementException
import org.jdbi.v3.sqlobject.kotlin.onDemand
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.postgresql.util.PSQLException
import sh.zachwal.dailygames.db.DatabaseExtension
import sh.zachwal.dailygames.db.jdbi.User
import sh.zachwal.dailygames.db.jdbi.puzzle.Game
import sh.zachwal.dailygames.db.jdbi.puzzle.Puzzle
import sh.zachwal.dailygames.db.jdbi.puzzle.WorldleResult
import java.time.Instant
import java.time.LocalDate
import kotlin.streams.toList

@ExtendWith(DatabaseExtension::class)
class WorldleDAOTest(jdbi: Jdbi) {

    private val userDAO: UserDAO = jdbi.onDemand()
    private lateinit var user: User

    private val puzzleDAO: PuzzleDAO = jdbi.onDemand()

    private val puzzleOne = Puzzle(Game.WORLDLE, 933, LocalDate.of(2024, 8, 11))
    private val puzzleTwo = Puzzle(Game.WORLDLE, 934, LocalDate.of(2024, 8, 12))

    private val worldleDAO: WorldleDAO = jdbi.onDemand()

    @BeforeEach
    fun addFixtures() {
        user = userDAO.createUser("zach", "hashedPassword")!!
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

        val result = worldleDAO.insertWorldleResult(
            userId = user.id,
            puzzle = puzzleOne,
            score = 5,
            shareText = shareText.trimIndent(),
            scorePercentage = 100
        )

        assertThat(result.userId).isEqualTo(user.id)
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
            worldleDAO.insertWorldleResult(
                userId = user.id,
                puzzle = puzzleOne,
                score = 5,
                shareText = "",
                scorePercentage = -1
            )
        }

        assertThrows<UnableToExecuteStatementException> {
            worldleDAO.insertWorldleResult(
                userId = user.id,
                puzzle = puzzleOne,
                score = 5,
                shareText = "",
                scorePercentage = 101
            )
        }
    }

    @Test
    fun `can retrieve worldle result for user on a date`() {
        val shareText = """
            #Worldle #934 (12.08.2024) 4/6 (100%)
            🟩🟩🟩🟩🟨⬅️
            🟩🟩🟩🟩🟨⬅️
            🟩🟩🟩🟩🟨↗️
            🟩🟩🟩🟩🟩🎉
            """.trimIndent()

        worldleDAO.insertWorldleResult(
            userId = user.id,
            puzzle = puzzleOne,
            score = 5,
            shareText = shareText.trimIndent(),
            scorePercentage = 100
        )

        val result = worldleDAO.worldleResultForUserOnDate(user.id, LocalDate.of(2024, 8, 11))

        assertThat(result).isNotNull()
        assertThat(result!!.userId).isEqualTo(user.id)
        assertThat(result.game).isEqualTo(Game.WORLDLE)
        assertThat(result.puzzleNumber).isEqualTo(933)
        assertThat(result.puzzleDate).isEqualTo(LocalDate.of(2024, 8, 11))
        assertThat(result.instantSubmitted).isIn(Range.closed(Instant.now().minusSeconds(10), Instant.now()))
        assertThat(result.score).isEqualTo(5)
        assertThat(result.shareText).isEqualTo(shareText)
        assertThat(result.scorePercentage).isEqualTo(100)
    }

    @Test
    fun `returns null when no result for user on date`() {
        val result = worldleDAO.worldleResultForUserOnDate(user.id, LocalDate.of(2024, 8, 11))

        assertThat(result).isNull()
    }
}