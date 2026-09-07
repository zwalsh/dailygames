package sh.zachwal.dailygames.results.gamemapper

import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.Test
import sh.zachwal.dailygames.db.jdbi.User
import sh.zachwal.dailygames.db.jdbi.puzzle.Game
import sh.zachwal.dailygames.db.jdbi.puzzle.PuzzleResult
import sh.zachwal.dailygames.leaderboard.PointCalculator
import sh.zachwal.dailygames.results.resultinfo.FlagleInfo
import java.time.Instant
import java.time.LocalDate

class FlagleMapperTest : GameMapperContractTest() {
    override val mapper = FlagleMapper(PointCalculator())
    override val fixtures = FlagleFixtures.ALL
    private val testUser = User(id = 1L, username = "test", hashedPassword = "hash")

    private val flagleResult = PuzzleResult(
        id = 1,
        userId = 1,
        game = Game.FLAGLE,
        puzzleNumber = 123,
        puzzleDate = null,
        instantSubmitted = Instant.now(),
        score = 1,
        shareText = "share text",
        resultInfo = FlagleInfo,
    )

    @Test
    fun `extracts Flagle info`() {
        val flagleInfo = mapper.extract(FlagleFixtures.FAILED, testUser)

        assertThat(flagleInfo.puzzleNumber).isEqualTo(905)
        assertThat(flagleInfo.date).isEqualTo(LocalDate.of(2024, 8, 14))
        assertThat(flagleInfo.score).isEqualTo(7)
        assertThat(flagleInfo.game).isEqualTo(Game.FLAGLE)
        assertThat(flagleInfo.resultInfo).isInstanceOf(FlagleInfo::class.java)
    }

    @Test
    fun `extracts Flagle info with one guess`() {
        val flagleInfo = mapper.extract(FlagleFixtures.ONE_GUESS, testUser)

        assertThat(flagleInfo.puzzleNumber).isEqualTo(905)
        assertThat(flagleInfo.date).isEqualTo(LocalDate.of(2024, 8, 14))
        assertThat(flagleInfo.score).isEqualTo(2)
    }

    @Test
    fun `maps flagle line`() {
        assertThat(mapper.shareLine(flagleResult.copy(score = 2))).isEqualTo("${Game.FLAGLE.emoji()} Flagle #123 2/6")
    }

    @Test
    fun `maps flagle perfect`() {
        assertThat(mapper.shareLine(flagleResult.copy(score = 1))).isEqualTo("${Game.FLAGLE.emoji()} Flagle #123 1/6 ${Game.FLAGLE.perfectEmoji()}")
    }

    @Test
    fun `maps flagle fail`() {
        assertThat(mapper.shareLine(flagleResult.copy(score = 7))).isEqualTo("${Game.FLAGLE.emoji()} Flagle #123 X/6")
    }
}
