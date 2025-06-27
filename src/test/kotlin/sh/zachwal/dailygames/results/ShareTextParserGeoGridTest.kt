package sh.zachwal.dailygames.results

import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.Test
import sh.zachwal.dailygames.db.jdbi.puzzle.Game
import sh.zachwal.dailygames.results.resultinfo.GeoGridInfo

const val GEOGRID_PERFECT = """
✅ ✅ ✅
✅ ✅ ✅
✅ ✅ ✅

🌎Game Summary🌎
Board #280
Score: 123.3
Rank: 3,618 / 11,718
https://geogridgame.com
@geogridgame
"""

const val GEOGRID_ZERO = """
❌ ❌ ❌
❌ ❌ ❌
❌ ❌ ❌

🌎Game Summary🌎
Board #280
Score: 900
Rank: 10,188 / 11,737
https://geogridgame.com
@geogridgame
"""

const val GEOGRID_SIX = """
✅ ✅ ✅
❌ ❌ ✅
✅ ✅ ❌

🌎Game Summary🌎
Board #280
Score: 382.7
Rank: 9,311 / 11,761
https://geogridgame.com
@geogridgame
"""

const val GEOGRID_INFINITE = """
✅ ✅ ✅
✅ ✅ ✅
✅ ✅ ✅

🌎Game Summary🌎
Board #280
Score: 88.9
Rank: 1,521 / 11,795
https://geogridgame.com
@geogridgame
"""

const val GEOGRID_NEW_FORMAT = """
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

const val GEOGRID_NEW_FORMAT_WITH_ROCKETS = """
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

const val GEOGRID_NEW_FORMAT_INFINITY_ON = """
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

class ShareTextParserGeoGridTest {
    private val parser = ShareTextParser()

    @Test
    fun `matches geogrid`() {
        assertThat(parser.identifyGame(GEOGRID_PERFECT)).isEqualTo(Game.GEOGRID)
        assertThat(parser.identifyGame(GEOGRID_ZERO)).isEqualTo(Game.GEOGRID)
        assertThat(parser.identifyGame(GEOGRID_SIX)).isEqualTo(Game.GEOGRID)
        assertThat(parser.identifyGame(GEOGRID_INFINITE)).isEqualTo(Game.GEOGRID)
        assertThat(parser.identifyGame(GEOGRID_NEW_FORMAT)).isEqualTo(Game.GEOGRID)
        assertThat(parser.identifyGame(GEOGRID_NEW_FORMAT_WITH_ROCKETS)).isEqualTo(Game.GEOGRID)
        assertThat(parser.identifyGame(GEOGRID_NEW_FORMAT_INFINITY_ON)).isEqualTo(Game.GEOGRID)
    }

    @Test
    fun `extracts geogrid perfect`() {
        val result = parser.extractGeoGridInfo(GEOGRID_PERFECT)
        assertThat(result.puzzleNumber).isEqualTo(280)
        assertThat(result.score).isEqualTo(9)
        assertThat(result.shareTextNoLink).isEqualTo(
            """
                ✅ ✅ ✅
                ✅ ✅ ✅
                ✅ ✅ ✅
                Score: 123.3
                Rank: 3,618 / 11,718
            """.trimIndent()
        )
        val info = result.info<GeoGridInfo>()
        assertThat(info.score).isEqualTo(123.3)
        assertThat(info.rank).isEqualTo(3618)
        assertThat(info.rankOutOf).isEqualTo(11718)
        assertThat(info.numCorrect).isEqualTo(9)
    }

    @Test
    fun `extracts geogrid zero`() {
        val result = parser.extractGeoGridInfo(GEOGRID_ZERO)
        assertThat(result.score).isEqualTo(0)
        assertThat(result.shareTextNoLink).isEqualTo(
            """
                ❌ ❌ ❌
                ❌ ❌ ❌
                ❌ ❌ ❌
                Score: 900
                Rank: 10,188 / 11,737
            """.trimIndent()
        )
        val info = result.info<GeoGridInfo>()
        assertThat(info.score).isEqualTo(900.0)
        assertThat(info.rank).isEqualTo(10188)
        assertThat(info.rankOutOf).isEqualTo(11737)
        assertThat(info.numCorrect).isEqualTo(0)
    }

    @Test
    fun `extracts geogrid six`() {
        val result = parser.extractGeoGridInfo(GEOGRID_SIX)
        assertThat(result.score).isEqualTo(6)
        assertThat(result.shareTextNoLink).isEqualTo(
            """
                ✅ ✅ ✅
                ❌ ❌ ✅
                ✅ ✅ ❌
                Score: 382.7
                Rank: 9,311 / 11,761
            """.trimIndent()
        )
        val info = result.info<GeoGridInfo>()
        assertThat(info.score).isEqualTo(382.7)
        assertThat(info.rank).isEqualTo(9311)
        assertThat(info.rankOutOf).isEqualTo(11761)
        assertThat(info.numCorrect).isEqualTo(6)
    }

    @Test
    fun `extracts geogrid infinite`() {
        val result = parser.extractGeoGridInfo(GEOGRID_INFINITE)
        assertThat(result.score).isEqualTo(9)
        assertThat(result.shareTextNoLink).isEqualTo(
            """
                ✅ ✅ ✅
                ✅ ✅ ✅
                ✅ ✅ ✅
                Score: 88.9
                Rank: 1,521 / 11,795
            """.trimIndent()
        )
        val info = result.info<GeoGridInfo>()
        assertThat(info.score).isEqualTo(88.9)
        assertThat(info.rank).isEqualTo(1521)
        assertThat(info.rankOutOf).isEqualTo(11795)
        assertThat(info.numCorrect).isEqualTo(9)
    }

    @Test
    fun `extracts new format geogrid with basic data`() {
        val result = parser.extractGeoGridInfo(GEOGRID_NEW_FORMAT)
        assertThat(result.puzzleNumber).isEqualTo(447)
        assertThat(result.score).isEqualTo(5) // Count of 🟩 emojis
        assertThat(result.shareTextNoLink).isEqualTo("""
            ❌🟩🟩
            🟩🟩❌
            ❌🟩❌
            Score: 460.8
            Rank: 4,287/5,102
            Peak Performance 🚀 | ★★★
            ♾️ Mode: Off
        """.trimIndent())
        
        val info = result.info<GeoGridInfo>()
        assertThat(info.score).isEqualTo(460.8)
        assertThat(info.rank).isEqualTo(4287)
        assertThat(info.rankOutOf).isEqualTo(5102)
        assertThat(info.numCorrect).isEqualTo(5)
        assertThat(info.grid).isEqualTo("❌🟩🟩\n🟩🟩❌\n❌🟩❌")
        assertThat(info.performanceDescription).isEqualTo("Peak Performance 🚀 | ★★★")
        assertThat(info.infinityModeOff).isEqualTo(true)
        assertThat(info.rocketCount).isEqualTo(1)
    }

    @Test
    fun `extracts new format geogrid with rocket count`() {
        val result = parser.extractGeoGridInfo(GEOGRID_NEW_FORMAT_WITH_ROCKETS)
        assertThat(result.puzzleNumber).isEqualTo(448)
        assertThat(result.score).isEqualTo(9) // Count of 🟩 emojis
        
        val info = result.info<GeoGridInfo>()
        assertThat(info.rocketCount).isEqualTo(2)
        assertThat(info.performanceDescription).isEqualTo("Elite Among Mortals 🎖️ 🚀🚀")
    }

    @Test 
    fun `extracts new format geogrid with infinity mode on`() {
        val result = parser.extractGeoGridInfo(GEOGRID_NEW_FORMAT_INFINITY_ON)
        assertThat(result.puzzleNumber).isEqualTo(449)
        assertThat(result.score).isEqualTo(5) // Count of 🟩 emojis
        
        val info = result.info<GeoGridInfo>()
        assertThat(info.infinityModeOff).isEqualTo(false)
        assertThat(info.performanceDescription).isEqualTo("Getting Warmer 🔥")
    }
}
