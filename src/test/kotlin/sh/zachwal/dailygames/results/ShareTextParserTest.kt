package sh.zachwal.dailygames.results

import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.Test
import sh.zachwal.dailygames.db.jdbi.puzzle.Game
import java.time.LocalDate

const val TRAVLE_PERFECT = """
#travle #607 +0 (Perfect)
✅✅✅✅✅✅✅
https://travle.earth
"""

const val TRAVLE_PLUS_0 = """
#travle #607 +0
✅✅✅🟩✅✅✅
"""

const val TRAVLE_WITH_HINT = """
#travle #606 +2 (1 hint)
✅✅🟩🟧🟧✅
"""

const val TRAVLE_THREE_AWAY = """
#travle #614 (3 away)
🟧🟥🟥🟥🟧🟥🟥🟥✅
https://travle.earth
"""

const val TOP5 = """
Top 5 #171
⬜🟧🟨⬜⬜🟩⬜⬜
"""

const val TOP5_ALL_5_WITH_MISSES = """
Top 5 #170
🟥⬜🟩🟨🟦⬜⬜⬜🟧
"""

const val TOP5_NO_MISSES = """
Top 5 #169
🟥🟩🟧🟦🟨
"""

const val TOP5_PERFECT = """
Top 5 #169
🟥🟧🟨🟩🟦
"""

const val FLAGLE = """
#Flagle #905 (14.08.2024) X/6
🟥🟥🟥
🟥🟥🟥
https://www.flagle.io
"""

const val FLAGLE_ONE_GUESS = """
#Flagle #905 (14.08.2024) 2/6
🟥🟩🟩
🟩🟩🟩
https://www.flagle.io
"""

const val PINPOINT_THREE = """
Pinpoint #126
🤔 🤔 📌 ⬜ ⬜ (3/5)
lnkd.in/pinpoint
"""

const val PINPOINT_FAIL = """
Pinpoint #123
🤔 🤔 🤔 🤔 🤔 (X/5)
lnkd.in/pinpoint.
"""

const val PINPOINT_NO_LINK = """
Pinpoint #126
🤔 🤔 📌 ⬜ ⬜ (3/5)
"""

class ShareTextParserTest {

    private val parser = ShareTextParser()

    // Text matching

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
    fun `matches Travle`() {
        assertThat(parser.identifyGame(TRAVLE_PERFECT)).isEqualTo(Game.TRAVLE)
        assertThat(parser.identifyGame(TRAVLE_PLUS_0)).isEqualTo(Game.TRAVLE)
        assertThat(parser.identifyGame(TRAVLE_WITH_HINT)).isEqualTo(Game.TRAVLE)
    }

    @Test
    fun `matches Top5`() {
        assertThat(parser.identifyGame(TOP5)).isEqualTo(Game.TOP5)
        assertThat(parser.identifyGame(TOP5_NO_MISSES)).isEqualTo(Game.TOP5)
        assertThat(parser.identifyGame(TOP5_PERFECT)).isEqualTo(Game.TOP5)
    }

    @Test
    fun `matches Flagle`() {
        assertThat(parser.identifyGame(FLAGLE)).isEqualTo(Game.FLAGLE)
        assertThat(parser.identifyGame(FLAGLE_ONE_GUESS)).isEqualTo(Game.FLAGLE)
    }

    @Test
    fun `matches Pinpoint`() {
        assertThat(parser.identifyGame(PINPOINT_THREE)).isEqualTo(Game.PINPOINT)
        assertThat(parser.identifyGame(PINPOINT_FAIL)).isEqualTo(Game.PINPOINT)
        assertThat(parser.identifyGame(PINPOINT_NO_LINK)).isEqualTo(Game.PINPOINT)
    }

    @Test
    fun `matches text with carriage return`() {
        val shareText = "#Tradle #890 X/6\r\nhttps://oec.world/en/games/tradle"

        assertThat(parser.identifyGame(shareText)).isEqualTo(Game.TRADLE)
    }

    // Extraction

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
    fun `extracts X score as 7`() {
        val shareText = """
            #Worldle #934 (12.08.2024) X/6 (100%)
            🟩🟩🟩🟩🟨⬅️
            🟩🟩🟩🟩🟨⬅️
            🟩🟩🟩🟩🟨↗️
            🟩🟩🟩🟩🟩🎉

            https://worldle.teuteuf.fr
        """.trimIndent()

        val worldleInfo = parser.extractWorldleInfo(shareText)

        assertThat(worldleInfo.score).isEqualTo(7)
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
        assertThat(tradleInfo.score).isEqualTo(7)
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

    @Test
    fun `extracts Travle info when perfect`() {
        val travleInfo = parser.extractTravleInfo(TRAVLE_PERFECT)

        assertThat(travleInfo.puzzleNumber).isEqualTo(607)
        assertThat(travleInfo.score).isEqualTo(0)
        assertThat(travleInfo.shareTextNoLink).isEqualTo(
            """
                #travle #607 +0 (Perfect)
                ✅✅✅✅✅✅✅
            """.trimIndent()
        )
        assertThat(travleInfo.numPerfect).isEqualTo(7)
        assertThat(travleInfo.numIncorrect).isEqualTo(0)
        assertThat(travleInfo.numGuesses).isEqualTo(7)
        assertThat(travleInfo.numHints).isEqualTo(0)
    }

    @Test
    fun `extracts Travle info when score is not perfect`() {
        val travleInfo = parser.extractTravleInfo(TRAVLE_PLUS_0)

        assertThat(travleInfo.puzzleNumber).isEqualTo(607)
        assertThat(travleInfo.score).isEqualTo(0)
        assertThat(travleInfo.shareTextNoLink).isEqualTo(
            """
                #travle #607 +0
                ✅✅✅🟩✅✅✅
            """.trimIndent()
        )
        assertThat(travleInfo.numPerfect).isEqualTo(6)
        assertThat(travleInfo.numIncorrect).isEqualTo(0)
        assertThat(travleInfo.numGuesses).isEqualTo(7)
        assertThat(travleInfo.numHints).isEqualTo(0)
    }

    @Test
    fun `extracts Travle info when hints are used`() {
        val travleInfo = parser.extractTravleInfo(TRAVLE_WITH_HINT)

        assertThat(travleInfo.puzzleNumber).isEqualTo(606)
        assertThat(travleInfo.score).isEqualTo(2)
        assertThat(travleInfo.shareTextNoLink).isEqualTo(
            """
                #travle #606 +2 (1 hint)
                ✅✅🟩🟧🟧✅
            """.trimIndent()
        )
        assertThat(travleInfo.numPerfect).isEqualTo(3)
        assertThat(travleInfo.numIncorrect).isEqualTo(2)
        assertThat(travleInfo.numGuesses).isEqualTo(6)
        assertThat(travleInfo.numHints).isEqualTo(1)
    }

    @Test
    fun `extracts Travle info when did not finish`() {
        val travleInfo = parser.extractTravleInfo(TRAVLE_THREE_AWAY)

        assertThat(travleInfo.puzzleNumber).isEqualTo(614)
        assertThat(travleInfo.score).isEqualTo(-3)
        assertThat(travleInfo.shareTextNoLink).isEqualTo(
            """
                #travle #614 (3 away)
                🟧🟥🟥🟥🟧🟥🟥🟥✅
            """.trimIndent()
        )
        assertThat(travleInfo.numPerfect).isEqualTo(1)
        assertThat(travleInfo.numIncorrect).isEqualTo(8)
        assertThat(travleInfo.numGuesses).isEqualTo(9)
        assertThat(travleInfo.numHints).isEqualTo(0)
    }

    @Test
    fun `extracts Top5 info`() {
        val top5Info = parser.extractTop5Info(TOP5)

        assertThat(top5Info.puzzleNumber).isEqualTo(171)
        assertThat(top5Info.shareTextNoLink).isEqualTo(
            """
                Top 5 #171
                ⬜🟧🟨⬜⬜🟩⬜⬜
            """.trimIndent()
        )
        assertThat(top5Info.score).isEqualTo(3)
        assertThat(top5Info.numGuesses).isEqualTo(8)
        assertThat(top5Info.numCorrect).isEqualTo(3)
        assertThat(top5Info.isPerfect).isFalse()
    }

    @Test
    fun `extracts Top5 info with all 5 correct but misses`() {
        val top5Info = parser.extractTop5Info(TOP5_ALL_5_WITH_MISSES)

        assertThat(top5Info.puzzleNumber).isEqualTo(170)
        assertThat(top5Info.shareTextNoLink).isEqualTo(
            """
                Top 5 #170
                🟥⬜🟩🟨🟦⬜⬜⬜🟧
            """.trimIndent()
        )
        assertThat(top5Info.score).isEqualTo(6)
        assertThat(top5Info.numGuesses).isEqualTo(9)
        assertThat(top5Info.numCorrect).isEqualTo(5)
        assertThat(top5Info.isPerfect).isFalse()
    }

    @Test
    fun `extracts Top5 info with no misses`() {
        val top5Info = parser.extractTop5Info(TOP5_NO_MISSES)

        assertThat(top5Info.puzzleNumber).isEqualTo(169)
        assertThat(top5Info.shareTextNoLink).isEqualTo(
            """
                Top 5 #169
                🟥🟩🟧🟦🟨
            """.trimIndent()
        )
        assertThat(top5Info.score).isEqualTo(10)
        assertThat(top5Info.numGuesses).isEqualTo(5)
        assertThat(top5Info.numCorrect).isEqualTo(5)
        assertThat(top5Info.isPerfect).isFalse()
    }

    @Test
    fun `extracts Top5 info with perfect score`() {
        val top5Info = parser.extractTop5Info(TOP5_PERFECT)

        assertThat(top5Info.puzzleNumber).isEqualTo(169)
        assertThat(top5Info.shareTextNoLink).isEqualTo(
            """
                Top 5 #169
                🟥🟧🟨🟩🟦
            """.trimIndent()
        )
        assertThat(top5Info.score).isEqualTo(10)
        assertThat(top5Info.numGuesses).isEqualTo(5)
        assertThat(top5Info.numCorrect).isEqualTo(5)
        assertThat(top5Info.isPerfect).isTrue()
    }

    @Test
    fun `extracts Flagle info`() {
        val flagleInfo = parser.extractFlagleInfo(FLAGLE)

        assertThat(flagleInfo.puzzleNumber).isEqualTo(905)
        assertThat(flagleInfo.date).isEqualTo(LocalDate.of(2024, 8, 14))
        assertThat(flagleInfo.score).isEqualTo(7)
        assertThat(flagleInfo.shareTextNoLink).isEqualTo(
            """
                #Flagle #905 (14.08.2024) X/6
                🟥🟥🟥
                🟥🟥🟥
            """.trimIndent()
        )
    }

    @Test
    fun `extracts Flagle info with one guess`() {
        val flagleInfo = parser.extractFlagleInfo(FLAGLE_ONE_GUESS)

        assertThat(flagleInfo.puzzleNumber).isEqualTo(905)
        assertThat(flagleInfo.date).isEqualTo(LocalDate.of(2024, 8, 14))
        assertThat(flagleInfo.score).isEqualTo(2)
        assertThat(flagleInfo.shareTextNoLink).isEqualTo(
            """
                #Flagle #905 (14.08.2024) 2/6
                🟥🟩🟩
                🟩🟩🟩
            """.trimIndent()
        )
    }
}
