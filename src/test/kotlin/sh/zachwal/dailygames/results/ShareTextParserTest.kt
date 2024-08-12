package sh.zachwal.dailygames.results

import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import sh.zachwal.dailygames.db.jdbi.puzzle.Game

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
    fun `does not match non-Worldle game`() {
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

        assertThat(parser.identifyGame(tradleText)).isNull()
    }
}