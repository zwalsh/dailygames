package sh.zachwal.dailygames.results

import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.Test
import sh.zachwal.dailygames.db.jdbi.puzzle.Game
import sh.zachwal.dailygames.results.resultinfo.BandleInfo

const val BANDLE_PERFECT = """
Bandle #941 1/6
🟩⬜⬜⬜⬜⬜
Found: 1/1 (100%)
#Bandle #Heardle #Wordle 

https://bandle.app/
"""

const val BANDLE_ZERO = """
Bandle #941 x/6
🟥🟥🟥🟥🟥🟥
Found: 0/1 (0%)
#Bandle #Heardle #Wordle 

https://bandle.app/
"""

const val BANDLE_FOUR = """
Bandle #941 4/6
🟨🟥🟨🟩⬜⬜
Found: 1/1 (100%)
#Bandle #Heardle #Wordle 

https://bandle.app/
"""

const val BANDLE_SKIP = """
Bandle #941 x/6
⬛⬛⬛⬛⬛⬛
Found: 0/1 (0%)
#Bandle #Heardle #Wordle 

https://bandle.app/
"""

class ShareTextParserBandleTest {
    private val parser = ShareTextParser()

    @Test
    fun `matches bandle`() {
        assertThat(parser.identifyGame(BANDLE_PERFECT)).isEqualTo(Game.BANDLE)
        assertThat(parser.identifyGame(BANDLE_ZERO)).isEqualTo(Game.BANDLE)
        assertThat(parser.identifyGame(BANDLE_FOUR)).isEqualTo(Game.BANDLE)
        assertThat(parser.identifyGame(BANDLE_SKIP)).isEqualTo(Game.BANDLE)
    }

    @Test
    fun `extracts bandle perfect`() {
        val parsed = parser.extractBandleInfo(BANDLE_PERFECT)
        assertThat(parsed.game).isEqualTo(Game.BANDLE)
        assertThat(parsed.score).isEqualTo(1)
        assertThat(parsed.puzzleNumber).isEqualTo(941)
        assertThat(parsed.shareTextNoLink).isEqualTo(
            """
                Bandle #941 1/6
                🟩⬜⬜⬜⬜⬜
            """.trimIndent()
        )

        assertThat(parsed.resultInfo).isInstanceOf(BandleInfo::class.java)
        val info = parsed.info<BandleInfo>()
        assertThat(info.numSkips).isEqualTo(0)
        assertThat(info.numCorrectBand).isEqualTo(0)
        assertThat(info.numIncorrect).isEqualTo(0)
    }

    @Test
    fun `extracts bandle zero`() {
        val parsed = parser.extractBandleInfo(BANDLE_ZERO)
        assertThat(parsed.game).isEqualTo(Game.BANDLE)
        assertThat(parsed.score).isEqualTo(7)
        assertThat(parsed.puzzleNumber).isEqualTo(941)
        assertThat(parsed.shareTextNoLink).isEqualTo(
            """
                Bandle #941 x/6
                🟥🟥🟥🟥🟥🟥
            """.trimIndent()
        )

        assertThat(parsed.resultInfo).isInstanceOf(BandleInfo::class.java)
        val info = parsed.info<BandleInfo>()
        assertThat(info.numSkips).isEqualTo(0)
        assertThat(info.numCorrectBand).isEqualTo(0)
        assertThat(info.numIncorrect).isEqualTo(6)
    }

    @Test
    fun `extracts bandle four`() {
        val parsed = parser.extractBandleInfo(BANDLE_FOUR)
        assertThat(parsed.game).isEqualTo(Game.BANDLE)
        assertThat(parsed.score).isEqualTo(4)
        assertThat(parsed.puzzleNumber).isEqualTo(941)
        assertThat(parsed.shareTextNoLink).isEqualTo(
            """
                Bandle #941 4/6
                🟨🟥🟨🟩⬜⬜
            """.trimIndent()
        )

        assertThat(parsed.resultInfo).isInstanceOf(BandleInfo::class.java)
        val info = parsed.info<BandleInfo>()
        assertThat(info.numSkips).isEqualTo(0)
        assertThat(info.numCorrectBand).isEqualTo(2)
        assertThat(info.numIncorrect).isEqualTo(1)
    }

    @Test
    fun `extracts bandle skip`() {
        val parsed = parser.extractBandleInfo(BANDLE_SKIP)
        assertThat(parsed.game).isEqualTo(Game.BANDLE)
        assertThat(parsed.score).isEqualTo(7)
        assertThat(parsed.puzzleNumber).isEqualTo(941)
        assertThat(parsed.shareTextNoLink).isEqualTo(
            """
                Bandle #941 x/6
                ⬛⬛⬛⬛⬛⬛
            """.trimIndent()
        )

        assertThat(parsed.resultInfo).isInstanceOf(BandleInfo::class.java)
        val info = parsed.info<BandleInfo>()
        assertThat(info.numSkips).isEqualTo(6)
        assertThat(info.numCorrectBand).isEqualTo(0)
        assertThat(info.numIncorrect).isEqualTo(0)
    }
}
