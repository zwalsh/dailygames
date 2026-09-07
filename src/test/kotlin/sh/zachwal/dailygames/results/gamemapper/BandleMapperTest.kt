package sh.zachwal.dailygames.results.gamemapper

import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.Test
import sh.zachwal.dailygames.db.jdbi.User
import sh.zachwal.dailygames.db.jdbi.puzzle.Game
import sh.zachwal.dailygames.db.jdbi.puzzle.PuzzleResult
import sh.zachwal.dailygames.leaderboard.PointCalculator
import sh.zachwal.dailygames.results.resultinfo.BandleInfo
import java.time.Instant

class BandleMapperTest : GameMapperContractTest() {
    override val mapper = BandleMapper(PointCalculator())
    override val fixtures = BandleFixtures.ALL
    private val testUser = User(id = 1L, username = "test", hashedPassword = "hash")

    private val bandleResult = PuzzleResult(
        id = 1,
        userId = 1,
        game = Game.BANDLE,
        puzzleNumber = 123,
        puzzleDate = null,
        instantSubmitted = Instant.now(),
        score = 1,
        shareText = "share text",
        resultInfo = BandleInfo(numSkips = 0, numCorrectBand = 0, numIncorrect = 0),
    )

    @Test
    fun `extracts bandle perfect`() {
        val parsed = mapper.extract(BandleFixtures.PERFECT, testUser)
        assertThat(parsed.game).isEqualTo(Game.BANDLE)
        assertThat(parsed.score).isEqualTo(1)
        assertThat(parsed.puzzleNumber).isEqualTo(941)
        val info = parsed.info<BandleInfo>()
        assertThat(info.numSkips).isEqualTo(0)
        assertThat(info.numCorrectBand).isEqualTo(0)
        assertThat(info.numIncorrect).isEqualTo(0)
    }

    @Test
    fun `extracts bandle zero`() {
        val parsed = mapper.extract(BandleFixtures.ZERO, testUser)
        assertThat(parsed.score).isEqualTo(7)
        val info = parsed.info<BandleInfo>()
        assertThat(info.numSkips).isEqualTo(0)
        assertThat(info.numCorrectBand).isEqualTo(0)
        assertThat(info.numIncorrect).isEqualTo(6)
    }

    @Test
    fun `extracts bandle four`() {
        val parsed = mapper.extract(BandleFixtures.FOUR, testUser)
        assertThat(parsed.score).isEqualTo(4)
        val info = parsed.info<BandleInfo>()
        assertThat(info.numSkips).isEqualTo(0)
        assertThat(info.numCorrectBand).isEqualTo(2)
        assertThat(info.numIncorrect).isEqualTo(1)
    }

    @Test
    fun `extracts bandle skip`() {
        val parsed = mapper.extract(BandleFixtures.SKIP, testUser)
        assertThat(parsed.score).isEqualTo(7)
        val info = parsed.info<BandleInfo>()
        assertThat(info.numSkips).isEqualTo(6)
        assertThat(info.numCorrectBand).isEqualTo(0)
        assertThat(info.numIncorrect).isEqualTo(0)
    }

    @Test
    fun `maps Bandle line`() {
        assertThat(mapper.shareLine(bandleResult)).isEqualTo("${Game.BANDLE.emoji()} Bandle #123 1/6 ${Game.BANDLE.perfectEmoji()}")
    }

    @Test
    fun `maps Bandle line zero`() {
        assertThat(mapper.shareLine(bandleResult.copy(score = 7))).isEqualTo("${Game.BANDLE.emoji()} Bandle #123 X/6")
    }
}
