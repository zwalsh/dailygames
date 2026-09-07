package sh.zachwal.dailygames.results.gamemapper

import com.google.common.truth.Truth.assertThat
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Test
import sh.zachwal.dailygames.db.jdbi.User
import sh.zachwal.dailygames.db.jdbi.puzzle.Game
import sh.zachwal.dailygames.db.jdbi.puzzle.PuzzleResult
import sh.zachwal.dailygames.results.resultinfo.SizeItUpInfo
import sh.zachwal.dailygames.users.UserPreferencesService
import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

class SizeItUpMapperTest : GameMapperContractTest() {
    private val date = LocalDate.of(2026, 9, 7)
    private val clock = Clock.fixed(
        date.atStartOfDay(ZoneId.of("America/New_York")).toInstant(),
        ZoneId.of("America/New_York"),
    )
    private val userPreferencesService = mockk<UserPreferencesService> {
        every { getTimeZone(any()) } returns ZoneId.of("America/New_York")
    }
    override val mapper = SizeItUpMapper(clock, userPreferencesService)
    override val fixtures = SizeItUpFixtures.ALL
    private val testUser = User(id = 1L, username = "test", hashedPassword = "hash")

    private val sizeItUpResult = PuzzleResult(
        id = 1,
        userId = 1,
        game = Game.SIZE_IT_UP,
        puzzleNumber = 20260907,
        puzzleDate = LocalDate.of(2026, 9, 7),
        instantSubmitted = Instant.now(),
        score = 300,
        shareText = "share text",
        resultInfo = SizeItUpInfo(roundScores = listOf(4, 10, 7, 5, 4)),
    )

    @Test
    fun `extracts Size It Up 300`() {
        val parsed = mapper.extract(SizeItUpFixtures.THREE_HUNDRED, testUser)
        assertThat(parsed.game).isEqualTo(Game.SIZE_IT_UP)
        assertThat(parsed.score).isEqualTo(300)
        assertThat(parsed.puzzleNumber).isEqualTo(20260907)
        assertThat(parsed.date).isEqualTo(date)
        assertThat(parsed.resultInfo).isEqualTo(SizeItUpInfo(roundScores = listOf(4, 10, 7, 5, 4)))
    }

    @Test
    fun `extracts Size It Up 351`() {
        val parsed = mapper.extract(SizeItUpFixtures.THREE_FIFTY_ONE, testUser)
        assertThat(parsed.score).isEqualTo(351)
        assertThat(parsed.resultInfo).isEqualTo(SizeItUpInfo(roundScores = listOf(8, 9, 5, 9, 4)))
    }

    @Test
    fun `extracts Size It Up 365 with no link in share text`() {
        val parsed = mapper.extract(SizeItUpFixtures.THREE_SIXTY_FIVE_NO_LINK, testUser)
        assertThat(parsed.score).isEqualTo(365)
        assertThat(parsed.resultInfo).isEqualTo(SizeItUpInfo(roundScores = listOf(5, 6, 9, 8, 8)))
    }

    @Test
    fun `extracts Size It Up perfect score`() {
        val parsed = mapper.extract(SizeItUpFixtures.PERFECT, testUser)
        assertThat(parsed.score).isEqualTo(500)
        assertThat(parsed.resultInfo).isEqualTo(SizeItUpInfo(roundScores = listOf(10, 10, 10, 10, 10)))
    }

    @Test
    fun `extracts Size It Up zero score`() {
        val parsed = mapper.extract(SizeItUpFixtures.ZERO, testUser)
        assertThat(parsed.score).isEqualTo(0)
        assertThat(parsed.resultInfo).isEqualTo(SizeItUpInfo(roundScores = listOf(0, 0, 0, 0, 0)))
    }

    @Test
    fun `maps Size It Up line`() {
        assertThat(mapper.shareLine(sizeItUpResult)).isEqualTo("${Game.SIZE_IT_UP.emoji()} Size It Up 9/07 300/500")
    }

    @Test
    fun `maps Size It Up perfect line`() {
        assertThat(mapper.shareLine(sizeItUpResult.copy(score = 500))).isEqualTo("${Game.SIZE_IT_UP.emoji()} Size It Up 9/07 500/500 ${Game.SIZE_IT_UP.perfectEmoji()}")
    }

    @Test
    fun `maps Size It Up zero line`() {
        assertThat(mapper.shareLine(sizeItUpResult.copy(score = 0))).isEqualTo("${Game.SIZE_IT_UP.emoji()} Size It Up 9/07 0/500")
    }
}
