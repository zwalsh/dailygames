package sh.zachwal.dailygames.answers

import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.Test
import sh.zachwal.dailygames.db.jdbi.puzzle.Game
import sh.zachwal.dailygames.db.jdbi.puzzle.Puzzle
import java.time.LocalDate


class WorldleAnswerServiceTest {
    private val service = WorldleAnswerService()

    @Test
    fun `provides the correct answer for 2025-01-06`() {
        val puzzle = Puzzle(Game.WORLDLE, 1081, LocalDate.of(2025, 1, 6))

        val answer = service.answerForPuzzle(puzzle)

        assertThat(answer).isEqualTo("United Arab Emirates \uD83C\uDDE6\uD83C\uDDEA")
    }
}