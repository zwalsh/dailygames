package sh.zachwal.dailygames.home

import com.google.common.truth.Truth.assertThat
import io.mockk.every
import io.mockk.mockk
import org.jdbi.v3.core.Jdbi
import org.jdbi.v3.sqlobject.kotlin.attach
import org.junit.jupiter.api.Test
import sh.zachwal.dailygames.db.dao.game.PuzzleResultDAO
import sh.zachwal.dailygames.db.jdbi.puzzle.Game
import sh.zachwal.dailygames.db.jdbi.puzzle.PuzzleResult
import sh.zachwal.dailygames.results.resultinfo.WorldleInfo
import sh.zachwal.dailygames.users.UserPreferencesService
import java.time.Clock
import java.time.Instant
import java.time.ZoneId
import java.time.temporal.ChronoUnit
import java.util.stream.Stream

class StreakServiceTest {

    private val userPreferencesService = mockk<UserPreferencesService> {
        every { getTimeZoneCached(any()) } returns ZoneId.of("America/New_York")
    }
    private val resultDAO = mockk<PuzzleResultDAO>()
    private val jdbi = mockk<Jdbi> {
        every { open() } returns mockk(relaxed = true) {
            every { attach<PuzzleResultDAO>() } returns resultDAO
        }
    }
    private val clock = mockk<Clock> {
        every { instant() } returns Instant.now()
    }

    private val streakService = StreakService(
        jdbi = jdbi,
        userPreferencesService = userPreferencesService,
        clock = clock,
    )

    private val instant = Instant.now()
    private val result = PuzzleResult(
        id = 1L,
        userId = 1L,
        game = Game.WORLDLE,
        score = 5,
        puzzleNumber = 943,
        puzzleDate = null,
        instantSubmitted = instant,
        shareText = "Share text",
        resultInfo = WorldleInfo(
            percentage = 100,
        ),
    )

    @Test
    fun `returns the number of consecutive days a user has played a game starting today or yesterday`() {
        every { resultDAO.resultsForUserSortedStream(any()) } returns Stream.of(
            result,
            result.copy(instantSubmitted = instant.minus(1, ChronoUnit.DAYS)),
            result.copy(instantSubmitted = instant.minus(2, ChronoUnit.DAYS)),
            result.copy(instantSubmitted = instant.minus(3, ChronoUnit.DAYS)),
            // gap
            result.copy(instantSubmitted = instant.minus(5, ChronoUnit.DAYS)),
        )

        val streak = streakService.streakForUser(1L)

        assertThat(streak).isEqualTo(4)
    }

    @Test
    fun `streak counts if you have not played yet today`() {
        every { resultDAO.resultsForUserSortedStream(any()) } returns Stream.of(
            result.copy(instantSubmitted = instant.minus(1, ChronoUnit.DAYS)),
            result.copy(instantSubmitted = instant.minus(2, ChronoUnit.DAYS)),
            result.copy(instantSubmitted = instant.minus(3, ChronoUnit.DAYS)),
        )

        val streak = streakService.streakForUser(1L)

        assertThat(streak).isEqualTo(3)
    }

    @Test
    fun `streak does not count if you did not play yesterday or today`() {
        every { resultDAO.resultsForUserSortedStream(any()) } returns Stream.of(
            result.copy(instantSubmitted = instant.minus(2, ChronoUnit.DAYS)),
            result.copy(instantSubmitted = instant.minus(3, ChronoUnit.DAYS)),
        )

        val streak = streakService.streakForUser(1L)

        assertThat(streak).isEqualTo(0)
    }

    @Test
    fun `streak does not extend if you play twice in one day`() {
        every { resultDAO.resultsForUserSortedStream(any()) } returns Stream.of(
            result,
            result.copy(instantSubmitted = instant.minus(1, ChronoUnit.DAYS)),
            result.copy(instantSubmitted = instant.minus(1, ChronoUnit.DAYS)),
            result.copy(instantSubmitted = instant.minus(2, ChronoUnit.DAYS)),
        )

        val streak = streakService.streakForUser(1L)

        assertThat(streak).isEqualTo(3)
    }

    @Test
    fun `respects the user's timezone`() {
        every { clock.instant() } returns Instant.ofEpochSecond(1736033167)

        // 12:01 yesterday ET, 9:01 two days ago PT
        val startOfYesterdayET = Instant.ofEpochSecond(1735880460)
        val result = result.copy(
            instantSubmitted = startOfYesterdayET,
        )
        every { resultDAO.resultsForUserSortedStream(any()) } answers { Stream.of(result) }

        // In PT, the streak is 0 (as the result is from 2 days ago)
        every { userPreferencesService.getTimeZoneCached(any()) } returns ZoneId.of("America/Los_Angeles")
        val streakPT = streakService.streakForUser(1L)
        assertThat(streakPT).isEqualTo(0)

        // In ET, the streak is 1 (as the result is from yesterday)
        every { userPreferencesService.getTimeZoneCached(any()) } returns ZoneId.of("America/New_York")
        val streakET = streakService.streakForUser(1L)
        assertThat(streakET).isEqualTo(1)
    }
}
