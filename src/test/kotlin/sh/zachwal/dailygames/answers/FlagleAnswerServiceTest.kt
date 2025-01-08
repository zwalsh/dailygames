package sh.zachwal.dailygames.answers

import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.Test
import sh.zachwal.dailygames.db.jdbi.puzzle.Game
import sh.zachwal.dailygames.db.jdbi.puzzle.Puzzle
import java.time.LocalDate


class FlagleAnswerServiceTest {

    private val service = FlagleAnswerService()

    @Test
    fun `returns the correct result for 2025-01-07`() {
        val puzzle = Puzzle(Game.FLAGLE, 1051, LocalDate.of(2025, 1, 7))

        val answer = service.answerForPuzzle(puzzle)

        assertThat(answer).isEqualTo("Bolivia \uD83C\uDDE7\uD83C\uDDF4")
    }
}