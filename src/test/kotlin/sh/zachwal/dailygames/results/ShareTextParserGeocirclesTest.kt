package sh.zachwal.dailygames.results

import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.Test
import sh.zachwal.dailygames.db.jdbi.puzzle.Game
import sh.zachwal.dailygames.results.resultinfo.GeocirclesInfo

const val GEOCIRCLES_PERFECT = """
Geocircles #55
🟢🟢🟢🟢🟢
❤️❤️❤️❤️❤️
https://geocircles.io/55
"""

const val GEOCIRCLES_0_POINTS = """
Geocircles #55
⚫⚫⚫⚫⚫
🖤🖤🖤🖤🖤
https://geocircles.io/55    
"""

const val GEOCIRCLES_DNF = """
Geocircles #55
🟢🟢🟢🟢⚫
🖤🖤🖤🖤🖤
https://geocircles.io/55   
"""

const val GEOCIRCLES_LIVES_LEFT = """
Geocircles #55
🟢🟢🟢🟢🟢
❤️❤️🖤🖤🖤
https://geocircles.io/55    
"""

class ShareTextParserGeocirclesTest {
    private val parser = ShareTextParser()

    @Test
    fun `matches geocircles`() {
        assertThat(parser.identifyGame(GEOCIRCLES_PERFECT)).isEqualTo(Game.GEOCIRCLES)
        assertThat(parser.identifyGame(GEOCIRCLES_0_POINTS)).isEqualTo(Game.GEOCIRCLES)
        assertThat(parser.identifyGame(GEOCIRCLES_DNF)).isEqualTo(Game.GEOCIRCLES)
        assertThat(parser.identifyGame(GEOCIRCLES_LIVES_LEFT)).isEqualTo(Game.GEOCIRCLES)
    }

    @Test
    fun `extracts geocircles`() {
        val info = parser.extractGeocirclesInfo(GEOCIRCLES_PERFECT)
        assertThat(info.puzzleNumber).isEqualTo(55)
        assertThat(info.score).isEqualTo(10)
        assertThat(info.shareTextNoLink).isEqualTo("Geocircles #55\n🟢🟢🟢🟢🟢\n❤️❤️❤️❤️❤️")
    }

    @Test
    fun `extracts geocircles 0 points`() {
        val info = parser.extractGeocirclesInfo(GEOCIRCLES_0_POINTS)
        assertThat(info.score).isEqualTo(0)
    }

    @Test
    fun `extracts geocircles did not finish`() {
        val info = parser.extractGeocirclesInfo(GEOCIRCLES_DNF)
        assertThat(info.score).isEqualTo(4)
    }

    @Test
    fun `extracts geocircles with lives left`() {
        val info = parser.extractGeocirclesInfo(GEOCIRCLES_LIVES_LEFT)
        assertThat(info.score).isEqualTo(7)
    }

    @Test
    fun `sets correct game info`() {
        val info = parser.extractGeocirclesInfo(GEOCIRCLES_PERFECT)
        assertThat(info.resultInfo).isEqualTo(GeocirclesInfo)
    }
}
