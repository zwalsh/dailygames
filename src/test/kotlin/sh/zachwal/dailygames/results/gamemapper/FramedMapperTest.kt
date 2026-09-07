package sh.zachwal.dailygames.results.gamemapper

import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.Test
import sh.zachwal.dailygames.db.jdbi.User
import sh.zachwal.dailygames.db.jdbi.puzzle.Game
import sh.zachwal.dailygames.db.jdbi.puzzle.PuzzleResult
import sh.zachwal.dailygames.leaderboard.PointCalculator
import sh.zachwal.dailygames.results.resultinfo.FramedInfo
import java.time.Instant

class FramedMapperTest : GameMapperContractTest() {
    override val mapper = FramedMapper(PointCalculator())
    override val fixtures = FramedFixtures.ALL
    private val testUser = User(id = 1L, username = "test", hashedPassword = "hash")

    private val framedResult = PuzzleResult(
        id = 1,
        userId = 1,
        game = Game.FRAMED,
        puzzleNumber = 123,
        puzzleDate = null,
        instantSubmitted = Instant.now(),
        score = 2,
        shareText = "share text",
        resultInfo = FramedInfo,
    )

    @Test
    fun `extracts framed`() {
        val info = mapper.extract(FramedFixtures.PERFECT, testUser)
        assertThat(info.puzzleNumber).isEqualTo(990)
        assertThat(info.score).isEqualTo(1)
        assertThat(info.shareTextNoLink).isEqualTo("Framed #990\n🎥 🟩 ⬛ ⬛ ⬛ ⬛ ⬛")
    }

    @Test
    fun `extracts framed 0 points`() {
        val info = mapper.extract(FramedFixtures.ZERO, testUser)
        assertThat(info.score).isEqualTo(7) // Total miss becomes score of 7
    }

    @Test
    fun `extracts framed 4 points`() {
        val info = mapper.extract(FramedFixtures.FOUR, testUser)
        assertThat(info.score).isEqualTo(4)
    }

    @Test
    fun `extracts framed 6 points`() {
        val info = mapper.extract(FramedFixtures.SIX, testUser)
        assertThat(info.score).isEqualTo(6)
    }

    @Test
    fun `sets correct game info`() {
        val info = mapper.extract(FramedFixtures.PERFECT, testUser)
        assertThat(info.resultInfo).isEqualTo(FramedInfo)
    }

    @Test
    fun `maps framed line`() {
        assertThat(mapper.shareLine(framedResult)).isEqualTo("${Game.FRAMED.emoji()} Framed #123 2/6")
    }

    @Test
    fun `maps framed line perfect`() {
        assertThat(mapper.shareLine(framedResult.copy(score = 1))).isEqualTo("${Game.FRAMED.emoji()} Framed #123 1/6 ${Game.FRAMED.perfectEmoji()}")
    }

    @Test
    fun `maps framed line fail`() {
        assertThat(mapper.shareLine(framedResult.copy(score = 7))).isEqualTo("${Game.FRAMED.emoji()} Framed #123 X/6")
    }
}
