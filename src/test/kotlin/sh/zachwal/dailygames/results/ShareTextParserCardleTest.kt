package sh.zachwal.dailygames.results

import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.Test
import sh.zachwal.dailygames.db.jdbi.puzzle.Game
import sh.zachwal.dailygames.results.resultinfo.CardleInfo
import java.time.LocalDate

const val CARDLE_PERFECT = """
Cardle 1/5
Streak 1🔥
Total Score 15
🟢 🟢 🟢
https://www.playcardle.com
"""

const val CARDLE_MID = """
Cardle 4/5
Streak 1🔥
Total Score 6
🔴 🔴 🔴
🟢 🔴 🔴
🟢 🟢 🔴
🟢 🟢 🟢
https://www.playcardle.com
"""

const val CARDLE_LOW_SCORE_NO_STREAK = """
Cardle 5/5
Total Score 1
🔴 🔴 🟢
🔴 🔴 🟢
🔴 🔴 🟢
🔴 🔴 🟢
🔴 🔴 🟢
https://www.playcardle.com
"""

const val CARDLE_FAILURE = """
Cardle 5/5

🔴 🔴 🔴
🔴 🔴 🔴
🔴 🔴 🔴
🔴 🔴 🔴
🔴 🔴 🔴
https://www.playcardle.com
"""

class ShareTextParserCardleTest {
    private val parser = ShareTextParser()
    private val date = LocalDate.of(2026, 9, 7)

    @Test
    fun `matches Cardle`() {
        assertThat(parser.identifyGame(CARDLE_PERFECT)).isEqualTo(Game.CARDLE)
        assertThat(parser.identifyGame(CARDLE_MID)).isEqualTo(Game.CARDLE)
        assertThat(parser.identifyGame(CARDLE_LOW_SCORE_NO_STREAK)).isEqualTo(Game.CARDLE)
        assertThat(parser.identifyGame(CARDLE_FAILURE)).isEqualTo(Game.CARDLE)
    }

    @Test
    fun `extracts Cardle perfect score`() {
        val parsed = parser.extractCardleInfo(CARDLE_PERFECT, date)

        assertThat(parsed.game).isEqualTo(Game.CARDLE)
        assertThat(parsed.score).isEqualTo(15)
        assertThat(parsed.puzzleNumber).isEqualTo(20260907)
        assertThat(parsed.date).isEqualTo(date)
        assertThat(parsed.shareTextNoLink).isEqualTo(
            """
                Cardle 1/5
                Streak 1🔥
                Total Score 15
                🟢 🟢 🟢
            """.trimIndent(),
        )
        assertThat(parsed.resultInfo).isEqualTo(CardleInfo(numGuesses = 1, streak = 1))
    }

    @Test
    fun `extracts Cardle mid-range score`() {
        val parsed = parser.extractCardleInfo(CARDLE_MID, date)

        assertThat(parsed.score).isEqualTo(6)
        assertThat(parsed.puzzleNumber).isEqualTo(20260907)
        assertThat(parsed.resultInfo).isEqualTo(CardleInfo(numGuesses = 4, streak = 1))
    }

    @Test
    fun `extracts Cardle low score with no streak line`() {
        val parsed = parser.extractCardleInfo(CARDLE_LOW_SCORE_NO_STREAK, date)

        assertThat(parsed.score).isEqualTo(1)
        assertThat(parsed.puzzleNumber).isEqualTo(20260907)
        assertThat(parsed.resultInfo).isEqualTo(CardleInfo(numGuesses = 5, streak = 0))
    }

    @Test
    fun `extracts Cardle failure with no streak or total score line`() {
        val parsed = parser.extractCardleInfo(CARDLE_FAILURE, date)

        assertThat(parsed.score).isEqualTo(0)
        assertThat(parsed.puzzleNumber).isEqualTo(20260907)
        assertThat(parsed.resultInfo).isEqualTo(CardleInfo(numGuesses = 5, streak = 0))
    }
}
