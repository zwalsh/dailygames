package sh.zachwal.dailygames.results.gamemapper

import com.google.common.truth.Truth.assertThat
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Test
import sh.zachwal.dailygames.db.jdbi.User
import sh.zachwal.dailygames.db.jdbi.puzzle.Game
import sh.zachwal.dailygames.db.jdbi.puzzle.PuzzleResult
import sh.zachwal.dailygames.results.resultinfo.MapTapInfo
import sh.zachwal.dailygames.results.resultinfo.ParsedResult
import sh.zachwal.dailygames.users.UserPreferencesService
import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

class MapTapMapperTest : GameMapperContractTest() {
    private val instant = Instant.parse("2024-09-11T05:00:00Z") // 1am 9/11 ET
    private val clock = Clock.fixed(instant, ZoneId.of("America/New_York"))
    private val userPreferencesService = mockk<UserPreferencesService> {
        every { getTimeZone(any()) } returns ZoneId.of("America/New_York")
    }
    override val mapper = MapTapMapper(clock, userPreferencesService)
    override val fixtures = MapTapFixtures.ALL
    private val testUser = User(id = 1L, username = "test", hashedPassword = "hash")

    @Test
    fun `extracts MapTap example result`() {
        val parsed = mapper.extract(MapTapFixtures.EXAMPLE, testUser)

        assertThat(parsed.game).isEqualTo(Game.MAPTAP)
        assertThat(parsed.date).isEqualTo(LocalDate.of(2024, 9, 7))
        assertThat(parsed.puzzleNumber).isEqualTo(20240907)
        assertThat(parsed.score).isEqualTo(797)
        assertThat(parsed.resultInfo).isEqualTo(
            MapTapInfo(
                finalScore = 797,
                roundScores = listOf(100, 93, 95, 66, 72),
            ),
        )
    }

    @Test
    fun `extracts MapTap result with a perfect round`() {
        val parsed = mapper.extract(MapTapFixtures.WITH_PERFECT_ROUND, testUser)

        assertThat(parsed.date).isEqualTo(LocalDate.of(2024, 5, 4))
        assertThat(parsed.score).isEqualTo(820)
        assertThat(parsed.resultInfo).isEqualTo(
            MapTapInfo(
                finalScore = 820,
                roundScores = listOf(100, 99, 78, 83, 72),
            ),
        )
    }

    @Test
    fun `extracts MapTap result with no perfect round`() {
        val parsed = mapper.extract(MapTapFixtures.NO_PERFECT_ROUND, testUser)

        assertThat(parsed.score).isEqualTo(942)
        assertThat(parsed.resultInfo).isEqualTo(
            MapTapInfo(
                finalScore = 942,
                roundScores = listOf(98, 99, 86, 98, 93),
            ),
        )
    }

    @Test
    fun `extracts MapTap result without the www prefix`() {
        val parsed = mapper.extract(MapTapFixtures.NO_WWW_PREFIX, testUser)

        assertThat(parsed.date).isEqualTo(LocalDate.of(2024, 9, 6))
        assertThat(parsed.score).isEqualTo(994)
        assertThat(parsed.resultInfo).isEqualTo(
            MapTapInfo(
                finalScore = 994,
                roundScores = listOf(100, 100, 100, 100, 98),
            ),
        )
    }

    @Test
    fun `maps MapTap share line with a perfect round`() {
        val parsed = mapper.extract(MapTapFixtures.WITH_PERFECT_ROUND, testUser)
        val result = puzzleResult(parsed)

        assertThat(mapper.shareLine(result)).isEqualTo(
            "${Game.MAPTAP.emoji()} MapTap 5/04 820/1000 ${Game.MAPTAP.perfectEmoji()}",
        )
    }

    @Test
    fun `maps MapTap share line with no perfect round`() {
        val parsed = mapper.extract(MapTapFixtures.NO_PERFECT_ROUND, testUser)
        val result = puzzleResult(parsed)

        assertThat(mapper.shareLine(result)).isEqualTo("${Game.MAPTAP.emoji()} MapTap 5/04 942/1000")
    }

    private fun puzzleResult(parsed: ParsedResult) = PuzzleResult(
        id = 1,
        userId = 1,
        game = Game.MAPTAP,
        puzzleNumber = parsed.puzzleNumber,
        puzzleDate = parsed.date,
        instantSubmitted = Instant.now(),
        score = parsed.score,
        shareText = parsed.shareTextNoLink,
        resultInfo = parsed.resultInfo,
    )
}
