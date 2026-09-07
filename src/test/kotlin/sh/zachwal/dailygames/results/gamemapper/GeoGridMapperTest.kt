package sh.zachwal.dailygames.results.gamemapper

import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.Test
import sh.zachwal.dailygames.db.jdbi.User
import sh.zachwal.dailygames.db.jdbi.puzzle.Game
import sh.zachwal.dailygames.db.jdbi.puzzle.PuzzleResult
import sh.zachwal.dailygames.results.resultinfo.GeoGridInfo
import java.time.Instant

class GeoGridMapperTest : GameMapperContractTest() {
    override val mapper = GeoGridMapper()
    override val fixtures = GeoGridFixtures.ALL
    private val testUser = User(id = 1L, username = "test", hashedPassword = "hash")

    private val geoGridResult = PuzzleResult(
        id = 1,
        userId = 1,
        game = Game.GEOGRID,
        puzzleNumber = 123,
        puzzleDate = null,
        instantSubmitted = Instant.now(),
        score = 9,
        shareText = "share text",
        resultInfo = GeoGridInfo(score = 123.4, rank = 123, rankOutOf = 5555, numCorrect = 9),
    )

    @Test
    fun `extracts geogrid perfect`() {
        val result = mapper.extract(GeoGridFixtures.PERFECT, testUser)
        assertThat(result.puzzleNumber).isEqualTo(280)
        assertThat(result.score).isEqualTo(9)
        val info = result.info<GeoGridInfo>()
        assertThat(info.score).isEqualTo(123.3)
        assertThat(info.rank).isEqualTo(3618)
        assertThat(info.rankOutOf).isEqualTo(11718)
        assertThat(info.numCorrect).isEqualTo(9)
    }

    @Test
    fun `extracts geogrid zero`() {
        val result = mapper.extract(GeoGridFixtures.ZERO, testUser)
        assertThat(result.score).isEqualTo(0)
        val info = result.info<GeoGridInfo>()
        assertThat(info.score).isEqualTo(900.0)
        assertThat(info.rank).isEqualTo(10188)
        assertThat(info.rankOutOf).isEqualTo(11737)
        assertThat(info.numCorrect).isEqualTo(0)
    }

    @Test
    fun `extracts geogrid six`() {
        val result = mapper.extract(GeoGridFixtures.SIX, testUser)
        assertThat(result.score).isEqualTo(6)
        val info = result.info<GeoGridInfo>()
        assertThat(info.score).isEqualTo(382.7)
        assertThat(info.rank).isEqualTo(9311)
        assertThat(info.rankOutOf).isEqualTo(11761)
        assertThat(info.numCorrect).isEqualTo(6)
    }

    @Test
    fun `extracts geogrid infinite`() {
        val result = mapper.extract(GeoGridFixtures.INFINITE, testUser)
        assertThat(result.score).isEqualTo(9)
        val info = result.info<GeoGridInfo>()
        assertThat(info.score).isEqualTo(88.9)
        assertThat(info.rank).isEqualTo(1521)
        assertThat(info.rankOutOf).isEqualTo(11795)
        assertThat(info.numCorrect).isEqualTo(9)
    }

    @Test
    fun `maps geogrid line`() {
        assertThat(mapper.shareLine(geoGridResult)).isEqualTo("${Game.GEOGRID.emoji()} GeoGrid #123 9/9 (123.4) ${Game.GEOGRID.perfectEmoji()}")
    }

    @Test
    fun `maps geogrid line zero`() {
        val result = geoGridResult.copy(score = 0, resultInfo = geoGridResult.info<GeoGridInfo>().copy(numCorrect = 0, score = 900.0))
        assertThat(mapper.shareLine(result)).isEqualTo("${Game.GEOGRID.emoji()} GeoGrid #123 0/9 (900.0)")
    }

    @Test
    fun `maps geogrid line 8 of 9`() {
        val result = geoGridResult.copy(score = 8, resultInfo = geoGridResult.info<GeoGridInfo>().copy(numCorrect = 8, score = 200.5))
        assertThat(mapper.shareLine(result)).isEqualTo("${Game.GEOGRID.emoji()} GeoGrid #123 8/9 (200.5)")
    }

    @Test
    fun `keeps max one decimal point of geogrid score`() {
        val result = geoGridResult.copy(resultInfo = geoGridResult.info<GeoGridInfo>().copy(score = 200.55))
        assertThat(mapper.shareLine(result)).contains("(200.6)")
    }
}
