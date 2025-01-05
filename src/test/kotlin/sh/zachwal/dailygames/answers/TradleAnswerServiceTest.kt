package sh.zachwal.dailygames.answers

import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.Test
import sh.zachwal.dailygames.db.jdbi.puzzle.Game
import sh.zachwal.dailygames.db.jdbi.puzzle.Puzzle
import java.time.LocalDate

class TradleAnswerServiceTest {

    private val service = TradleAnswerService()

    @Test
    fun `gives correct answer for 2025-01-01`() {
        val answer = service.answerForDate(LocalDate.of(2025, 1, 5))
        assertThat(answer?.displayCountry).isEqualTo("Ghana")
    }

    @Test
    fun `gives correct answer for puzzle number 1036 (also 2025-01-01)`() {
        val puzzle = Puzzle(
            game = Game.TRADLE,
            number = 1036,
            date = null,
        )
        val answer = service.answerForPuzzle(puzzle)
        assertThat(answer).isEqualTo("Ghana \uD83C\uDDEC\uD83C\uDDED")
    }
}