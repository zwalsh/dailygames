package sh.zachwal.dailygames.results

import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.Test
import sh.zachwal.dailygames.db.jdbi.puzzle.Game
import java.time.LocalDate

class ShareTextParserTest {

    private val parser = ShareTextParser()

    @Test
    fun `can match worldle`() {
        val shareText = """
            #Worldle #934 (12.08.2024) 4/6 (100%)
            🟩🟩🟩🟩🟨⬅️
            🟩🟩🟩🟩🟨⬅️
            🟩🟩🟩🟩🟨↗️
            🟩🟩🟩🟩🟩🎉

            https://worldle.teuteuf.fr
        """.trimIndent()

        assertThat(parser.identifyGame(shareText)).isEqualTo(Game.WORLDLE)
    }

    @Test
    fun `matches with preceding and trailing whitespace`() {
        val shareText = """
            
            
            #Worldle #934 (12.08.2024) 4/6 (100%)
            🟩🟩🟩🟩🟨⬅️
            🟩🟩🟩🟩🟨⬅️
            🟩🟩🟩🟩🟨↗️
            🟩🟩🟩🟩🟩🎉

            https://worldle.teuteuf.fr
            
            
        """.trimIndent()

        assertThat(parser.identifyGame(shareText)).isEqualTo(Game.WORLDLE)
    }

    @Test
    fun `matches even if result is X out of 6`() {
        val shareText = """
            #Worldle #934 (12.08.2024) X/6 (100%)
            🟩🟩🟩🟩🟨⬅️
            🟩🟩🟩🟩🟨⬅️
            🟩🟩🟩🟩🟨↗️
            🟩🟩🟩🟩🟩🎉

            https://worldle.teuteuf.fr
        """.trimIndent()

        assertThat(parser.identifyGame(shareText)).isEqualTo(Game.WORLDLE)
    }

    @Test
    fun `matches Tradle`() {
        val tradleText = """
            #Tradle #890 X/6
            🟩🟩⬜⬜⬜
            🟩🟩🟩🟩⬜
            🟩🟩🟩🟩🟨
            🟩🟩🟩🟩🟨
            🟩🟩🟩🟩🟨
            🟩🟩🟩🟩🟨
            https://oec.world/en/games/tradle
        """.trimIndent()

        assertThat(parser.identifyGame(tradleText)).isEqualTo(Game.TRADLE)
    }

    @Test
    fun `extracts Worldle info`() {
        val shareText = """
            #Worldle #934 (12.08.2024) 4/6 (100%)
            🟩🟩🟩🟩🟨⬅️
            🟩🟩🟩🟩🟨⬅️
            🟩🟩🟩🟩🟨↗️
            🟩🟩🟩🟩🟩🎉

            https://worldle.teuteuf.fr
        """.trimIndent()

        val worldleInfo = parser.extractWorldleInfo(shareText)

        assertThat(worldleInfo.puzzleNumber).isEqualTo(934)
        assertThat(worldleInfo.date).isEqualTo(LocalDate.of(2024, 8, 12))
        assertThat(worldleInfo.score).isEqualTo(4)
        assertThat(worldleInfo.percentage).isEqualTo(100)
        assertThat(worldleInfo.shareTextNoLink).isEqualTo(
            """
            #Worldle #934 (12.08.2024) 4/6 (100%)
            🟩🟩🟩🟩🟨⬅️
            🟩🟩🟩🟩🟨⬅️
            🟩🟩🟩🟩🟨↗️
            🟩🟩🟩🟩🟩🎉
            """.trimIndent()
        )
    }

    @Test
    fun `extracts X score as 0`() {
        val shareText = """
            #Worldle #934 (12.08.2024) X/6 (100%)
            🟩🟩🟩🟩🟨⬅️
            🟩🟩🟩🟩🟨⬅️
            🟩🟩🟩🟩🟨↗️
            🟩🟩🟩🟩🟩🎉

            https://worldle.teuteuf.fr
        """.trimIndent()

        val worldleInfo = parser.extractWorldleInfo(shareText)

        assertThat(worldleInfo.score).isEqualTo(0)
    }

    @Test
    fun `extracts share text no link even if link not present`() {
        val shareText = """
            #Worldle #934 (12.08.2024) 4/6 (100%)
            🟩🟩🟩🟩🟨⬅️
            🟩🟩🟩🟩🟨⬅️
            🟩🟩🟩🟩🟨↗️
            🟩🟩🟩🟩🟩🎉
        """.trimIndent()

        val worldleInfo = parser.extractWorldleInfo(shareText)

        assertThat(worldleInfo.shareTextNoLink).isEqualTo(
            """
            #Worldle #934 (12.08.2024) 4/6 (100%)
            🟩🟩🟩🟩🟨⬅️
            🟩🟩🟩🟩🟨⬅️
            🟩🟩🟩🟩🟨↗️
            🟩🟩🟩🟩🟩🎉
            """.trimIndent()
        )
    }

    @Test
    fun `extracts Tradle info`() {
        val shareText = """
            #Tradle #890 X/6
            🟩🟩⬜⬜⬜
            🟩🟩🟩🟩⬜
            🟩🟩🟩🟩🟨
            🟩🟩🟩🟩🟨
            🟩🟩🟩🟩🟨
            🟩🟩🟩🟩🟨
            https://oec.world/en/games/tradle
        """.trimIndent()

        val tradleInfo = parser.extractTradleInfo(shareText)

        assertThat(tradleInfo.puzzleNumber).isEqualTo(890)
        assertThat(tradleInfo.score).isEqualTo(0)
        assertThat(tradleInfo.shareTextNoLink).isEqualTo(
            """
            #Tradle #890 X/6
            🟩🟩⬜⬜⬜
            🟩🟩🟩🟩⬜
            🟩🟩🟩🟩🟨
            🟩🟩🟩🟩🟨
            🟩🟩🟩🟩🟨
            🟩🟩🟩🟩🟨
            """.trimIndent()
        )
    }
}
