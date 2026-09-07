package sh.zachwal.dailygames.results.gamemapper

import com.google.common.truth.Truth.assertThat
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Test
import sh.zachwal.dailygames.db.jdbi.User
import sh.zachwal.dailygames.db.jdbi.puzzle.Game
import sh.zachwal.dailygames.db.jdbi.puzzle.PuzzleResult
import sh.zachwal.dailygames.results.resultinfo.CardleInfo
import sh.zachwal.dailygames.users.UserPreferencesService
import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

class CardleMapperTest : GameMapperContractTest() {
    private val date = LocalDate.of(2026, 9, 7)
    private val clock = Clock.fixed(
        date.atStartOfDay(ZoneId.of("America/New_York")).toInstant(),
        ZoneId.of("America/New_York"),
    )
    private val userPreferencesService = mockk<UserPreferencesService> {
        every { getTimeZone(any()) } returns ZoneId.of("America/New_York")
    }
    override val mapper = CardleMapper(clock, userPreferencesService)
    override val fixtures = CardleFixtures.ALL
    private val testUser = User(id = 1L, username = "test", hashedPassword = "hash")

    private val cardleResult = PuzzleResult(
        id = 1,
        userId = 1,
        game = Game.CARDLE,
        puzzleNumber = 20260907,
        puzzleDate = LocalDate.of(2026, 9, 7),
        instantSubmitted = Instant.now(),
        score = 15,
        shareText = "share text",
        resultInfo = CardleInfo(numGuesses = 1, streak = 1),
    )

    @Test
    fun `extracts Cardle perfect score`() {
        val parsed = mapper.extract(CardleFixtures.PERFECT, testUser)

        assertThat(parsed.game).isEqualTo(Game.CARDLE)
        assertThat(parsed.score).isEqualTo(15)
        assertThat(parsed.puzzleNumber).isEqualTo(20260907)
        assertThat(parsed.date).isEqualTo(date)
        assertThat(parsed.resultInfo).isEqualTo(CardleInfo(numGuesses = 1, streak = 1))
    }

    @Test
    fun `extracts Cardle mid-range score`() {
        val parsed = mapper.extract(CardleFixtures.MID, testUser)

        assertThat(parsed.score).isEqualTo(6)
        assertThat(parsed.resultInfo).isEqualTo(CardleInfo(numGuesses = 4, streak = 1))
    }

    @Test
    fun `extracts Cardle low score with no streak line`() {
        val parsed = mapper.extract(CardleFixtures.LOW_SCORE_NO_STREAK, testUser)

        assertThat(parsed.score).isEqualTo(1)
        assertThat(parsed.resultInfo).isEqualTo(CardleInfo(numGuesses = 5, streak = 0))
    }

    @Test
    fun `extracts Cardle failure with no streak or total score line`() {
        val parsed = mapper.extract(CardleFixtures.FAILURE, testUser)

        assertThat(parsed.score).isEqualTo(0)
        assertThat(parsed.resultInfo).isEqualTo(CardleInfo(numGuesses = 5, streak = 0))
    }

    @Test
    fun `maps Cardle perfect line`() {
        assertThat(mapper.shareLine(cardleResult)).isEqualTo("${Game.CARDLE.emoji()} Cardle 9/07 15/15 ${Game.CARDLE.perfectEmoji()}")
    }

    @Test
    fun `maps Cardle mid-range line`() {
        assertThat(mapper.shareLine(cardleResult.copy(score = 6))).isEqualTo("${Game.CARDLE.emoji()} Cardle 9/07 6/15")
    }

    @Test
    fun `maps Cardle failure line`() {
        assertThat(mapper.shareLine(cardleResult.copy(score = 0))).isEqualTo("${Game.CARDLE.emoji()} Cardle 9/07 0/15")
    }
}
