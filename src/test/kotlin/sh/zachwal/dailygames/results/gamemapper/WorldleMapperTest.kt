package sh.zachwal.dailygames.results.gamemapper

import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.Test
import sh.zachwal.dailygames.db.jdbi.User
import sh.zachwal.dailygames.db.jdbi.puzzle.Game
import sh.zachwal.dailygames.db.jdbi.puzzle.PuzzleResult
import sh.zachwal.dailygames.leaderboard.PointCalculator
import sh.zachwal.dailygames.results.resultinfo.WorldleInfo
import java.time.Instant
import java.time.LocalDate

class WorldleMapperTest : GameMapperContractTest() {
    override val mapper = WorldleMapper(PointCalculator())
    override val fixtures = WorldleFixtures.ALL
    private val testUser = User(id = 1L, username = "test", hashedPassword = "hash")

    private val worldleResult = PuzzleResult(
        id = 1,
        userId = 1,
        game = Game.WORLDLE,
        puzzleNumber = 123,
        puzzleDate = null,
        instantSubmitted = Instant.now(),
        score = 1,
        shareText = "share text",
        resultInfo = WorldleInfo(percentage = 100),
    )

    @Test
    fun `extracts Worldle info`() {
        val result = mapper.extract(WorldleFixtures.FOUR_OF_SIX, testUser)
        assertThat(result.resultInfo).isInstanceOf(WorldleInfo::class.java)
        assertThat(result.puzzleNumber).isEqualTo(934)
        assertThat(result.date).isEqualTo(LocalDate.of(2024, 8, 12))
        assertThat(result.score).isEqualTo(4)
        assertThat(result.shareTextNoLink).isEqualTo(
            """
            #Worldle #934 (12.08.2024) 4/6 (100%)
            🟩🟩🟩🟩🟨⬅️
            🟩🟩🟩🟩🟨⬅️
            🟩🟩🟩🟩🟨↗️
            🟩🟩🟩🟩🟩🎉
            """.trimIndent(),
        )
        assertThat(result.game).isEqualTo(Game.WORLDLE)
        assertThat(result.info<WorldleInfo>().percentage).isEqualTo(100)
    }

    @Test
    fun `extracts X score as 7`() {
        val result = mapper.extract(WorldleFixtures.FAILED, testUser)
        assertThat(result.score).isEqualTo(7)
    }

    @Test
    fun `maps worldle perfect`() {
        assertThat(mapper.shareLine(worldleResult)).isEqualTo("${Game.WORLDLE.emoji()} Worldle #123 1/6 ${Game.WORLDLE.perfectEmoji()}")
    }

    @Test
    fun `maps worldle non-perfect`() {
        assertThat(mapper.shareLine(worldleResult.copy(score = 4))).isEqualTo("${Game.WORLDLE.emoji()} Worldle #123 4/6")
    }

    @Test
    fun `for Worldle fail, includes percentage`() {
        val failResult = worldleResult.copy(score = 7, resultInfo = WorldleInfo(percentage = 50))
        assertThat(mapper.shareLine(failResult)).isEqualTo("${Game.WORLDLE.emoji()} Worldle #123 X/6 (50%)")
    }
}
