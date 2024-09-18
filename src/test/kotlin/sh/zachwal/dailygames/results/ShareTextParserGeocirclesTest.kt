package sh.zachwal.dailygames.results

import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.Test
import sh.zachwal.dailygames.db.jdbi.puzzle.Game

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

    }

    @Test
    fun `extracts geocircles 0 points`() {

    }

    @Test
    fun `extracts geocircles did not finish`() {

    }

    @Test
    fun `extracts geocircles with lives left`() {

    }
}