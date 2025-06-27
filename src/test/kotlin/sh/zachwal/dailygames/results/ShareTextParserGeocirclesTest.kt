package sh.zachwal.dailygames.results

import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.Test
import sh.zachwal.dailygames.db.jdbi.puzzle.Game
import sh.zachwal.dailygames.results.resultinfo.GeocirclesInfo
import sh.zachwal.dailygames.results.resultinfo.EnhancedGeocirclesInfo

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

const val GEOCIRCLES_NEW_FORMAT = """
Geocircles Board #447
❌🟩🟩
🟩🟩❌
❌🟩❌
Score: 460.8
Rank: 4,287/5,102
Peak Performance 🚀 | ★★★
♾️ Mode: Off
https://geocircles.io/447
"""

const val GEOCIRCLES_NEW_FORMAT_WITH_ROCKETS = """
Geocircles Board #448
🟩🟩🟩
🟩🟩🟩
🟩🟩🟩
Score: 892.5
Rank: 1,204/8,391
Elite Among Mortals 🎖️ 🚀🚀
♾️ Mode: Off
https://geocircles.io/448
"""

const val GEOCIRCLES_NEW_FORMAT_INFINITY_ON = """
Geocircles Board #449
❌❌🟩
🟩❌🟩
🟩🟩❌
Score: 234.1
Rank: 6,789/9,012
Getting Warmer 🔥
♾️ Mode: On
https://geocircles.io/449
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

    @Test
    fun `matches new format geocircles`() {
        assertThat(parser.identifyGame(GEOCIRCLES_NEW_FORMAT)).isEqualTo(Game.GEOCIRCLES)
        assertThat(parser.identifyGame(GEOCIRCLES_NEW_FORMAT_WITH_ROCKETS)).isEqualTo(Game.GEOCIRCLES)
        assertThat(parser.identifyGame(GEOCIRCLES_NEW_FORMAT_INFINITY_ON)).isEqualTo(Game.GEOCIRCLES)
    }

    @Test
    fun `extracts new format geocircles with basic data`() {
        val info = parser.extractGeocirclesInfo(GEOCIRCLES_NEW_FORMAT)
        assertThat(info.puzzleNumber).isEqualTo(447)
        assertThat(info.score).isEqualTo(460.8.toInt()) // Score should be converted to int for compatibility
        assertThat(info.shareTextNoLink).isEqualTo("""
            Geocircles Board #447
            ❌🟩🟩
            🟩🟩❌
            ❌🟩❌
            Score: 460.8
            Rank: 4,287/5,102
            Peak Performance 🚀 | ★★★
            ♾️ Mode: Off
        """.trimIndent())
        
        // Check enhanced info fields
        val enhancedInfo = info.resultInfo as EnhancedGeocirclesInfo
        assertThat(enhancedInfo.grid).isEqualTo("❌🟩🟩\n🟩🟩❌\n❌🟩❌")
        assertThat(enhancedInfo.numericScore).isEqualTo(460.8)
        assertThat(enhancedInfo.rank).isEqualTo("4,287/5,102")
        assertThat(enhancedInfo.performanceDescription).isEqualTo("Peak Performance 🚀 | ★★★")
        assertThat(enhancedInfo.boardNumber).isEqualTo(447)
        assertThat(enhancedInfo.infinityModeOff).isEqualTo(true)
        assertThat(enhancedInfo.rocketCount).isEqualTo(1)
    }

    @Test
    fun `extracts new format geocircles with rocket count`() {
        val info = parser.extractGeocirclesInfo(GEOCIRCLES_NEW_FORMAT_WITH_ROCKETS)
        assertThat(info.puzzleNumber).isEqualTo(448)
        assertThat(info.score).isEqualTo(892.5.toInt())
        
        val enhancedInfo = info.resultInfo as EnhancedGeocirclesInfo
        assertThat(enhancedInfo.rocketCount).isEqualTo(2)
        assertThat(enhancedInfo.performanceDescription).isEqualTo("Elite Among Mortals 🎖️ 🚀🚀")
    }

    @Test 
    fun `extracts new format geocircles with infinity mode on`() {
        val info = parser.extractGeocirclesInfo(GEOCIRCLES_NEW_FORMAT_INFINITY_ON)
        assertThat(info.puzzleNumber).isEqualTo(449)
        assertThat(info.score).isEqualTo(234.1.toInt())
        
        val enhancedInfo = info.resultInfo as EnhancedGeocirclesInfo
        assertThat(enhancedInfo.infinityModeOff).isEqualTo(false)
        assertThat(enhancedInfo.performanceDescription).isEqualTo("Getting Warmer 🔥")
    }
}
