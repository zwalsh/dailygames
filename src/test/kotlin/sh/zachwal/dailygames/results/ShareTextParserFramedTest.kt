package sh.zachwal.dailygames.results

import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.Test
import sh.zachwal.dailygames.db.jdbi.puzzle.Game
import sh.zachwal.dailygames.results.resultinfo.GeocirclesInfo

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

class ShareTextParserFramedTest {
    private val parser = ShareTextParser()

    @Test
    fun `matches framed`() {
        assertThat(parser.identifyGame(FRAMED_PERFECT)).isEqualTo(Game.FRAMED)
        assertThat(parser.identifyGame(FRAMED_ZERO)).isEqualTo(Game.FRAMED)
        assertThat(parser.identifyGame(FRAMED_FOUR)).isEqualTo(Game.FRAMED)
    }
//
//    @Test
//    fun `extracts framed`() {
//        val info = parser.extractGeocirclesInfo(GEOCIRCLES_PERFECT)
//        assertThat(info.puzzleNumber).isEqualTo(55)
//        assertThat(info.score).isEqualTo(10)
//        assertThat(info.shareTextNoLink).isEqualTo("Geocircles #55\n🟢🟢🟢🟢🟢\n❤️❤️❤️❤️❤️")
//    }
//
//    @Test
//    fun `extracts geocircles 0 points`() {
//        val info = parser.extractGeocirclesInfo(GEOCIRCLES_0_POINTS)
//        assertThat(info.score).isEqualTo(0)
//    }
//
//    @Test
//    fun `extracts geocircles did not finish`() {
//        val info = parser.extractGeocirclesInfo(GEOCIRCLES_DNF)
//        assertThat(info.score).isEqualTo(4)
//    }
//
//    @Test
//    fun `extracts geocircles with lives left`() {
//        val info = parser.extractGeocirclesInfo(GEOCIRCLES_LIVES_LEFT)
//        assertThat(info.score).isEqualTo(7)
//    }
//
//    @Test
//    fun `sets correct game info`() {
//        val info = parser.extractGeocirclesInfo(GEOCIRCLES_PERFECT)
//        assertThat(info.resultInfo).isEqualTo(GeocirclesInfo)
//    }
}
