package sh.zachwal.dailygames.results.gamemapper

import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.Test
import sh.zachwal.dailygames.db.jdbi.User
import sh.zachwal.dailygames.db.jdbi.puzzle.Game
import sh.zachwal.dailygames.db.jdbi.puzzle.PuzzleResult
import sh.zachwal.dailygames.leaderboard.PointCalculator
import sh.zachwal.dailygames.results.resultinfo.PinpointInfo
import java.time.Instant

class PinpointMapperTest : GameMapperContractTest() {
    override val mapper = PinpointMapper(PointCalculator())
    override val fixtures = PinpointFixtures.ALL
    private val testUser = User(id = 1L, username = "test", hashedPassword = "hash")

    private val pinpointResult = PuzzleResult(
        id = 1,
        userId = 1,
        game = Game.PINPOINT,
        puzzleNumber = 123,
        puzzleDate = null,
        instantSubmitted = Instant.now(),
        score = 1,
        shareText = "share text",
        resultInfo = PinpointInfo,
    )

    @Test
    fun `extracts Pinpoint info`() {
        val pinpointInfo = mapper.extract(PinpointFixtures.THREE, testUser)

        assertThat(pinpointInfo.puzzleNumber).isEqualTo(126)
        assertThat(pinpointInfo.score).isEqualTo(3)
        assertThat(pinpointInfo.game).isEqualTo(Game.PINPOINT)
        assertThat(pinpointInfo.resultInfo).isInstanceOf(PinpointInfo::class.java)
    }

    @Test
    fun `extracts Pinpoint info with X score`() {
        val pinpointInfo = mapper.extract(PinpointFixtures.FAIL, testUser)

        assertThat(pinpointInfo.puzzleNumber).isEqualTo(123)
        // X score is 6
        assertThat(pinpointInfo.score).isEqualTo(6)
    }

    @Test
    fun `extracts Pinpoint info with no link`() {
        val pinpointInfo = mapper.extract(PinpointFixtures.NO_LINK, testUser)

        assertThat(pinpointInfo.puzzleNumber).isEqualTo(126)
        assertThat(pinpointInfo.score).isEqualTo(3)
    }

    @Test
    fun `maps pinpoint perfect`() {
        assertThat(mapper.shareLine(pinpointResult)).isEqualTo("${Game.PINPOINT.emoji()} Pinpoint #123 1/5 ${Game.PINPOINT.perfectEmoji()}")
    }

    @Test
    fun `maps pinpoint non-perfect`() {
        assertThat(mapper.shareLine(pinpointResult.copy(score = 4))).isEqualTo("${Game.PINPOINT.emoji()} Pinpoint #123 4/5")
    }

    @Test
    fun `maps pinpoint fail`() {
        assertThat(mapper.shareLine(pinpointResult.copy(score = 6))).isEqualTo("${Game.PINPOINT.emoji()} Pinpoint #123 X/5")
    }
}
