package sh.zachwal.dailygames.results

import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.Test
import sh.zachwal.dailygames.db.jdbi.puzzle.Game
import sh.zachwal.dailygames.results.resultinfo.SizeItUpInfo
import java.time.LocalDate

const val SIZE_IT_UP_300 = """
Size It Up
Overall Score 300
🟥🟥🟥🟥⬜⬜⬜⬜⬜⬜
🟥🟥🟥🟥🟥🟥🟥🟥🟥🟥
🟥🟥🟥🟥🟥🟥🟥⬜⬜⬜
🟥🟥🟥🟥🟥⬜⬜⬜⬜⬜
🟥🟥🟥🟥⬜⬜⬜⬜⬜⬜
https://magnitudle.com/size-it-up
"""

const val SIZE_IT_UP_351 = """
Size It Up
Overall Score 351
🟥🟥🟥🟥🟥🟥🟥🟥⬜⬜
🟥🟥🟥🟥🟥🟥🟥🟥🟥⬜
🟥🟥🟥🟥🟥⬜⬜⬜⬜⬜
🟥🟥🟥🟥🟥🟥🟥🟥🟥⬜
🟥🟥🟥🟥⬜⬜⬜⬜⬜⬜
https://magnitudle.com/size-it-up
"""

const val SIZE_IT_UP_365 = """
Size It Up
Overall Score 365
🟥🟥🟥🟥🟥⬜⬜⬜⬜⬜
🟥🟥🟥🟥🟥🟥⬜⬜⬜⬜
🟥🟥🟥🟥🟥🟥🟥🟥🟥⬜
🟥🟥🟥🟥🟥🟥🟥🟥⬜⬜
🟥🟥🟥🟥🟥🟥🟥🟥⬜⬜
"""

const val SIZE_IT_UP_PERFECT = """
Size It Up
Overall Score 500
🟥🟥🟥🟥🟥🟥🟥🟥🟥🟥
🟥🟥🟥🟥🟥🟥🟥🟥🟥🟥
🟥🟥🟥🟥🟥🟥🟥🟥🟥🟥
🟥🟥🟥🟥🟥🟥🟥🟥🟥🟥
🟥🟥🟥🟥🟥🟥🟥🟥🟥🟥
https://magnitudle.com/size-it-up
"""

const val SIZE_IT_UP_ZERO = """
Size It Up
Overall Score 0
⬜⬜⬜⬜⬜⬜⬜⬜⬜⬜
⬜⬜⬜⬜⬜⬜⬜⬜⬜⬜
⬜⬜⬜⬜⬜⬜⬜⬜⬜⬜
⬜⬜⬜⬜⬜⬜⬜⬜⬜⬜
⬜⬜⬜⬜⬜⬜⬜⬜⬜⬜
https://magnitudle.com/size-it-up
"""

class ShareTextParserSizeItUpTest {
    private val parser = ShareTextParser()
    private val date = LocalDate.of(2026, 9, 7)

    @Test
    fun `matches Size It Up`() {
        assertThat(parser.identifyGame(SIZE_IT_UP_300)).isEqualTo(Game.SIZE_IT_UP)
        assertThat(parser.identifyGame(SIZE_IT_UP_351)).isEqualTo(Game.SIZE_IT_UP)
        assertThat(parser.identifyGame(SIZE_IT_UP_365)).isEqualTo(Game.SIZE_IT_UP)
        assertThat(parser.identifyGame(SIZE_IT_UP_PERFECT)).isEqualTo(Game.SIZE_IT_UP)
        assertThat(parser.identifyGame(SIZE_IT_UP_ZERO)).isEqualTo(Game.SIZE_IT_UP)
    }

    @Test
    fun `extracts Size It Up 300`() {
        val parsed = parser.extractSizeItUpInfo(SIZE_IT_UP_300, date)
        assertThat(parsed.game).isEqualTo(Game.SIZE_IT_UP)
        assertThat(parsed.score).isEqualTo(300)
        assertThat(parsed.puzzleNumber).isEqualTo(20260907)
        assertThat(parsed.date).isEqualTo(date)
        assertThat(parsed.shareTextNoLink).isEqualTo(
            """
                Size It Up
                Overall Score 300
                🟥🟥🟥🟥⬜⬜⬜⬜⬜⬜
                🟥🟥🟥🟥🟥🟥🟥🟥🟥🟥
                🟥🟥🟥🟥🟥🟥🟥⬜⬜⬜
                🟥🟥🟥🟥🟥⬜⬜⬜⬜⬜
                🟥🟥🟥🟥⬜⬜⬜⬜⬜⬜
            """.trimIndent(),
        )
        assertThat(parsed.resultInfo).isEqualTo(SizeItUpInfo(roundScores = listOf(4, 10, 7, 5, 4)))
    }

    @Test
    fun `extracts Size It Up 351`() {
        val parsed = parser.extractSizeItUpInfo(SIZE_IT_UP_351, date)
        assertThat(parsed.score).isEqualTo(351)
        assertThat(parsed.puzzleNumber).isEqualTo(20260907)
        assertThat(parsed.resultInfo).isEqualTo(SizeItUpInfo(roundScores = listOf(8, 9, 5, 9, 4)))
    }

    @Test
    fun `extracts Size It Up 365 with no link in share text`() {
        val parsed = parser.extractSizeItUpInfo(SIZE_IT_UP_365, date)
        assertThat(parsed.score).isEqualTo(365)
        assertThat(parsed.puzzleNumber).isEqualTo(20260907)
        assertThat(parsed.shareTextNoLink).isEqualTo(
            """
                Size It Up
                Overall Score 365
                🟥🟥🟥🟥🟥⬜⬜⬜⬜⬜
                🟥🟥🟥🟥🟥🟥⬜⬜⬜⬜
                🟥🟥🟥🟥🟥🟥🟥🟥🟥⬜
                🟥🟥🟥🟥🟥🟥🟥🟥⬜⬜
                🟥🟥🟥🟥🟥🟥🟥🟥⬜⬜
            """.trimIndent(),
        )
        assertThat(parsed.resultInfo).isEqualTo(SizeItUpInfo(roundScores = listOf(5, 6, 9, 8, 8)))
    }

    @Test
    fun `extracts Size It Up perfect score`() {
        val parsed = parser.extractSizeItUpInfo(SIZE_IT_UP_PERFECT, date)
        assertThat(parsed.score).isEqualTo(500)
        assertThat(parsed.puzzleNumber).isEqualTo(20260907)
        assertThat(parsed.resultInfo).isEqualTo(SizeItUpInfo(roundScores = listOf(10, 10, 10, 10, 10)))
    }

    @Test
    fun `extracts Size It Up zero score`() {
        val parsed = parser.extractSizeItUpInfo(SIZE_IT_UP_ZERO, date)
        assertThat(parsed.score).isEqualTo(0)
        assertThat(parsed.puzzleNumber).isEqualTo(20260907)
        assertThat(parsed.resultInfo).isEqualTo(SizeItUpInfo(roundScores = listOf(0, 0, 0, 0, 0)))
    }
}
