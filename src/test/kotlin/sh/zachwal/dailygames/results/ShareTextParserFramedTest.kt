package sh.zachwal.dailygames.results

import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.Test
import sh.zachwal.dailygames.db.jdbi.puzzle.Game
import sh.zachwal.dailygames.results.resultinfo.FramedInfo

const val FRAMED_PERFECT = """
Framed #990
🎥 🟩 ⬛ ⬛ ⬛ ⬛ ⬛

https://framed.wtf
"""

const val FRAMED_ZERO = """
Framed #990
🎥 🟥 🟥 🟥 🟥 🟥 🟥

https://framed.wtf
"""

const val FRAMED_FOUR = """
Framed #990
🎥 🟥 🟥 🟥 🟩 ⬛ ⬛

https://framed.wtf
"""

const val FRAMED_SIX = """
Framed #990
🎥 🟥 🟥 🟥 🟥 🟥 🟩

https://framed.wtf
"""

class ShareTextParserFramedTest {
    private val parser = ShareTextParser()

    @Test
    fun `matches framed`() {
        assertThat(parser.identifyGame(FRAMED_PERFECT)).isEqualTo(Game.FRAMED)
        assertThat(parser.identifyGame(FRAMED_ZERO)).isEqualTo(Game.FRAMED)
        assertThat(parser.identifyGame(FRAMED_FOUR)).isEqualTo(Game.FRAMED)
    }

    @Test
    fun `extracts framed`() {
        val info = parser.extractFramedInfo(FRAMED_PERFECT)
        assertThat(info.puzzleNumber).isEqualTo(990)
        assertThat(info.score).isEqualTo(1)
        assertThat(info.shareTextNoLink).isEqualTo("Framed #990\n🎥 🟩 ⬛ ⬛ ⬛ ⬛ ⬛")
    }

    @Test
    fun `extracts framed 0 points`() {
        val info = parser.extractFramedInfo(FRAMED_ZERO)
        assertThat(info.score).isEqualTo(7) // Total miss becomes score of 7
    }

    @Test
    fun `extracts framed 4 points`() {
        val info = parser.extractFramedInfo(FRAMED_FOUR)
        assertThat(info.score).isEqualTo(4)
    }

    @Test
    fun `extracts framed 6 points`() {
        val info = parser.extractFramedInfo(FRAMED_SIX)
        assertThat(info.score).isEqualTo(6)
    }

    @Test
    fun `sets correct game info`() {
        val info = parser.extractFramedInfo(FRAMED_PERFECT)
        assertThat(info.resultInfo).isEqualTo(FramedInfo)
    }
}
