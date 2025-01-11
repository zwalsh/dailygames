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

class ShareTextParserGeoGridTest {
    private val parser = ShareTextParser()

    @Test
    fun `matches geogrid`() {
        assertThat(parser.identifyGame(GEOGRID_PERFECT)).isEqualTo(Game.GEOGRID)
        assertThat(parser.identifyGame(GEOGRID_ZERO)).isEqualTo(Game.GEOGRID)
        assertThat(parser.identifyGame(GEOGRID_SIX)).isEqualTo(Game.GEOGRID)
        assertThat(parser.identifyGame(GEOGRID_INFINITE)).isEqualTo(Game.GEOGRID)
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

//    @Test
//    fun `extracts framed`() {
//        val info = parser.extractFramedInfo(FRAMED_PERFECT)
//        assertThat(info.puzzleNumber).isEqualTo(990)
//        assertThat(info.score).isEqualTo(1)
//        assertThat(info.shareTextNoLink).isEqualTo("Framed #990\n🎥 🟩 ⬛ ⬛ ⬛ ⬛ ⬛")
//    }
//
//    @Test
//    fun `extracts framed 0 points`() {
//        val info = parser.extractFramedInfo(FRAMED_ZERO)
//        assertThat(info.score).isEqualTo(7) // Total miss becomes score of 7
//    }
//
//    @Test
//    fun `extracts framed 4 points`() {
//        val info = parser.extractFramedInfo(FRAMED_FOUR)
//        assertThat(info.score).isEqualTo(4)
//    }
//
//    @Test
//    fun `extracts framed 6 points`() {
//        val info = parser.extractFramedInfo(FRAMED_SIX)
//        assertThat(info.score).isEqualTo(6)
//    }
//
//    @Test
//    fun `sets correct game info`() {
//        val info = parser.extractFramedInfo(FRAMED_PERFECT)
//        assertThat(info.resultInfo).isEqualTo(FramedInfo)
//    }
}
