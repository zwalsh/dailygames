package sh.zachwal.dailygames.db.dao.game

import com.google.common.truth.Truth.assertThat
import org.jdbi.v3.core.Jdbi
import org.jdbi.v3.sqlobject.kotlin.onDemand
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import sh.zachwal.dailygames.db.extension.DatabaseExtension
import sh.zachwal.dailygames.db.jdbi.puzzle.Game
import sh.zachwal.dailygames.db.jdbi.puzzle.Puzzle
import java.time.LocalDate
import kotlin.streams.toList

@ExtendWith(DatabaseExtension::class)
class PuzzleDAOTest(jdbi: Jdbi) {

    private val puzzleDAO: PuzzleDAO = jdbi.onDemand()

    @Test
    fun `can insert and retrieve a puzzle`() {
        val puzzle = Puzzle(Game.TRAVLE, 123, LocalDate.of(2024, 8, 11))
        puzzleDAO.insertPuzzle(puzzle)

        val queried = puzzleDAO.getPuzzle(Game.TRAVLE, 123)

        assertThat(queried).isEqualTo(puzzle)
    }

    @Test
    fun `returns inserted puzzle`() {
        val puzzle = Puzzle(Game.TRAVLE, 123, LocalDate.of(2024, 8, 11))
        val inserted = puzzleDAO.insertPuzzle(puzzle)

        assertThat(inserted).isEqualTo(puzzle)
    }

    @Test
    fun `cannot insert two puzzles of the same game and number`() {
        val puzzle = Puzzle(Game.TRAVLE, 123, LocalDate.of(2024, 8, 11))
        puzzleDAO.insertPuzzle(puzzle)

        assertThrows<Exception> {
            puzzleDAO.insertPuzzle(puzzle)
        }
    }

    @Test
    fun `can list puzzles by game`() {
        (1..5)
            .map { Puzzle(Game.TRAVLE, it, LocalDate.of(2024, 8, it)) }
            .forEach { puzzleDAO.insertPuzzle(it) }

        val puzzles = puzzleDAO.listPuzzlesForGameDescending(Game.TRAVLE).toList()

        assertThat(puzzles).hasSize(5)
        assertThat(puzzles.first()).isEqualTo(Puzzle(Game.TRAVLE, 5, LocalDate.of(2024, 8, 5)))
    }

    @Test
    fun `can check if puzzle exists`() {
        val puzzle = Puzzle(Game.TRAVLE, 123, LocalDate.of(2024, 8, 11))
        puzzleDAO.insertPuzzle(puzzle)

        val exists = puzzleDAO.puzzleExists(Game.TRAVLE, 123)

        assertThat(exists).isTrue()
    }

    @Test
    fun `can check if puzzle does not exist`() {
        val exists = puzzleDAO.puzzleExists(Game.TRAVLE, 123)

        assertThat(exists).isFalse()
    }

    @Test
    fun `query for previous puzzle`() {
        val puzzle = Puzzle(Game.TRAVLE, 123, null)
        val previous = puzzle.copy(number = 100)

        puzzleDAO.insertPuzzle(puzzle)
        puzzleDAO.insertPuzzle(previous)

        assertThat(puzzleDAO.previousPuzzle(Game.TRAVLE, 123)).isEqualTo(previous)
    }

    @Test
    fun `previousPuzzle returns null when none exists`() {
        val puzzle = Puzzle(Game.TRAVLE, 123, null)

        puzzleDAO.insertPuzzle(puzzle)

        assertThat(puzzleDAO.previousPuzzle(Game.TRAVLE, 123)).isNull()
    }

    @Test
    fun `query for next puzzle`() {
        val puzzle = Puzzle(Game.TRAVLE, 123, null)
        val next = puzzle.copy(number = 200)

        puzzleDAO.insertPuzzle(puzzle)
        puzzleDAO.insertPuzzle(next)

        assertThat(puzzleDAO.nextPuzzle(Game.TRAVLE, 123)).isEqualTo(next)
    }

    @Test
    fun `nextPuzzle returns null when none exists`() {
        val puzzle = Puzzle(Game.TRAVLE, 123, null)

        puzzleDAO.insertPuzzle(puzzle)

        assertThat(puzzleDAO.nextPuzzle(Game.TRAVLE, 123)).isNull()
    }

    @Test
    fun `latestPuzzlePerGame returns the latest puzzle by number for each game`() {
        // Fixtures mess with this, inserting later puzzles
        val puzzles = listOf(
            Puzzle(Game.TRAVLE, 1, LocalDate.of(2024, 8, 1)),
            Puzzle(Game.TRAVLE, 2, LocalDate.of(2024, 8, 2)),
            Puzzle(Game.TRAVLE, 3, LocalDate.of(2024, 8, 3)),
            Puzzle(Game.WORLDLE, 198, null),
            Puzzle(Game.WORLDLE, 199, null),
            Puzzle(Game.TOP5, 3, null),
            Puzzle(Game.TRADLE, 124, null),
            Puzzle(Game.TRADLE, 123, null),
            Puzzle(Game.FLAGLE, 201, null),
        )
        puzzles.forEach { puzzleDAO.insertPuzzle(it) }

        val latestPuzzles = puzzleDAO.latestPuzzlePerGame()

        assertThat(latestPuzzles).containsExactly(
            Puzzle(Game.TRAVLE, 3, LocalDate.of(2024, 8, 3)),
            Puzzle(Game.WORLDLE, 199, null),
            Puzzle(Game.TOP5, 3, null),
            Puzzle(Game.TRADLE, 124, null),
            Puzzle(Game.FLAGLE, 201, null),
        )
    }
}
