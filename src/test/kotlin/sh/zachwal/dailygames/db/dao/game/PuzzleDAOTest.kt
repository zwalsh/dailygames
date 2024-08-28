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
        val puzzle = Puzzle(Game.WORLDLE, 123, LocalDate.of(2024, 8, 11))
        puzzleDAO.insertPuzzle(puzzle)

        val queried = puzzleDAO.getPuzzle(Game.WORLDLE, 123)

        assertThat(queried).isEqualTo(puzzle)
    }

    @Test
    fun `returns inserted puzzle`() {
        val puzzle = Puzzle(Game.WORLDLE, 123, LocalDate.of(2024, 8, 11))
        val inserted = puzzleDAO.insertPuzzle(puzzle)

        assertThat(inserted).isEqualTo(puzzle)
    }

    @Test
    fun `cannot insert two puzzles of the same game and number`() {
        val puzzle = Puzzle(Game.WORLDLE, 123, LocalDate.of(2024, 8, 11))
        puzzleDAO.insertPuzzle(puzzle)

        assertThrows<Exception> {
            puzzleDAO.insertPuzzle(puzzle)
        }
    }

    @Test
    fun `can list puzzles by game`() {
        (1..5)
            .map { Puzzle(Game.WORLDLE, it, LocalDate.of(2024, 8, it)) }
            .forEach { puzzleDAO.insertPuzzle(it) }

        val puzzles = puzzleDAO.listPuzzlesForGameDescending(Game.WORLDLE).toList()

        assertThat(puzzles).hasSize(5)
        assertThat(puzzles.first()).isEqualTo(Puzzle(Game.WORLDLE, 5, LocalDate.of(2024, 8, 5)))
    }

    @Test
    fun `can check if puzzle exists`() {
        val puzzle = Puzzle(Game.WORLDLE, 123, LocalDate.of(2024, 8, 11))
        puzzleDAO.insertPuzzle(puzzle)

        val exists = puzzleDAO.puzzleExists(Game.WORLDLE, 123)

        assertThat(exists).isTrue()
    }

    @Test
    fun `can check if puzzle does not exist`() {
        val exists = puzzleDAO.puzzleExists(Game.WORLDLE, 123)

        assertThat(exists).isFalse()
    }
}
