package sh.zachwal.dailygames.results.gamemapper

import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.Test
import sh.zachwal.dailygames.db.jdbi.User
import sh.zachwal.dailygames.db.jdbi.puzzle.Game
import sh.zachwal.dailygames.db.jdbi.puzzle.PuzzleResult
import sh.zachwal.dailygames.leaderboard.PointCalculator
import sh.zachwal.dailygames.results.resultinfo.TradleInfo
import java.time.Instant

class TradleMapperTest : GameMapperContractTest() {
    override val mapper = TradleMapper(PointCalculator())
    override val fixtures = TradleFixtures.ALL
    private val testUser = User(id = 1L, username = "test", hashedPassword = "hash")

    private val tradleResult = PuzzleResult(
        id = 1,
        userId = 1,
        game = Game.TRADLE,
        puzzleNumber = 123,
        puzzleDate = null,
        instantSubmitted = Instant.now(),
        score = 1,
        shareText = "share text",
        resultInfo = TradleInfo,
    )

    @Test
    fun `extracts Tradle info`() {
        val tradleInfo = mapper.extract(TradleFixtures.FAILED, testUser)

        assertThat(tradleInfo.puzzleNumber).isEqualTo(890)
        assertThat(tradleInfo.score).isEqualTo(7)
        assertThat(tradleInfo.shareTextNoLink).isEqualTo(
            """
            #Tradle #890 X/6
            🟩🟩⬜⬜⬜
            🟩🟩🟩🟩⬜
            🟩🟩🟩🟩🟨
            🟩🟩🟩🟩🟨
            🟩🟩🟩🟩🟨
            🟩🟩🟩🟩🟨
            """.trimIndent(),
        )
        assertThat(tradleInfo.game).isEqualTo(Game.TRADLE)
    }

    @Test
    fun `maps tradle line`() {
        assertThat(mapper.shareLine(tradleResult.copy(score = 2))).isEqualTo("${Game.TRADLE.emoji()} Tradle #123 2/6")
    }

    @Test
    fun `maps tradle perfect`() {
        assertThat(mapper.shareLine(tradleResult.copy(score = 1))).isEqualTo("${Game.TRADLE.emoji()} Tradle #123 1/6 ${Game.TRADLE.perfectEmoji()}")
    }

    @Test
    fun `maps tradle fail`() {
        assertThat(mapper.shareLine(tradleResult.copy(score = 7))).isEqualTo("${Game.TRADLE.emoji()} Tradle #123 X/6")
    }
}
