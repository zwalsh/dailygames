package sh.zachwal.dailygames.utils

import com.google.common.truth.Truth.assertThat
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Test
import sh.zachwal.dailygames.users.UserPreferencesService
import java.time.Instant
import java.time.ZoneId
import java.time.temporal.ChronoUnit

class DisplayTimeTest {

    private val userPreferencesService = mockk<UserPreferencesService> {
        every { getTimeZone(any()) } returns ZoneId.of("America/New_York")
    }
    private val service = DisplayTimeService(userPreferencesService)

    @Test
    fun `displayTime returns just now for recent results`() {
        val now = Instant.ofEpochSecond(1724074018)
        val justNow = now.minusSeconds(1)

        assertThat(service.displayTime(justNow, 1L, now = now)).isEqualTo("Just now")
    }

    @Test
    fun `displayTime returns 1 minute ago for results 1 minute ago`() {
        val now = Instant.ofEpochSecond(1724074018)
        val oneMinuteAgo = now.minusSeconds(60)

        assertThat(service.displayTime(oneMinuteAgo, 1L, now = now)).isEqualTo("1m ago")
    }

    @Test
    fun `displayTime returns 30min ago for results 30min ago`() {
        val now = Instant.ofEpochSecond(1724074018)
        val thirtyMinutesAgo = now.minusSeconds(60 * 30)

        assertThat(service.displayTime(thirtyMinutesAgo, 1L, now = now)).isEqualTo("30m ago")
    }

    @Test
    fun `displayTime returns 1h ago for results 1h ago`() {
        val now = Instant.ofEpochSecond(1724074018)
        val oneHourAgo = now.minusSeconds(60 * 60)

        assertThat(service.displayTime(oneHourAgo, 1L, now = now)).isEqualTo("1h 0m ago")
    }

    @Test
    fun `displayTime returns combination of hours and minutes`() {
        val time = Instant.now().minusSeconds(60 * 90)

        assertThat(service.displayTime(time, 1L)).isEqualTo("1h 30m ago")
    }

    @Test
    fun `displayTime returns hours and minutes for up to 24 hours when same day`() {
        val midnightAug17 = Instant.ofEpochSecond(1723867200)
        val eleven59PMAug17 = Instant.ofEpochSecond(1723953540)

        assertThat(service.displayTime(midnightAug17, 1L, now = eleven59PMAug17)).isEqualTo("23h 59m ago")
    }

    @Test
    fun `displayTime returns yesterday when result is from yesterday`() {
        // These times are one minute apart
        val midnightAug17 = Instant.ofEpochSecond(1723867200)
        val eleven59PMAug16 = Instant.ofEpochSecond(1723867140)

        assertThat(service.displayTime(eleven59PMAug16, 1L, now = midnightAug17)).isEqualTo("Yesterday at 11:59PM ET")
    }

    @Test
    fun `displayTime returns yesterday when result is from yesterday and time is midnight`() {
        val midnightAug17 = Instant.ofEpochSecond(1723867200)
        val midnightAug16 = Instant.ofEpochSecond(1723780800)

        assertThat(service.displayTime(midnightAug16, 1L, now = midnightAug17)).isEqualTo("Yesterday at 12:00AM ET")
    }

    @Test
    fun `displayTime formats with day of week, month, day of month, time when from before yesterday`() {
        val midnightAug17 = Instant.ofEpochSecond(1723867200)
        val now = midnightAug17.plus(2, ChronoUnit.DAYS)

        assertThat(service.displayTime(midnightAug17, 1L, now)).isEqualTo("Sat Aug 17 at 12:00AM ET")
    }

    @Test
    fun `displayTime formats with user time zone`() {
        // 11:59PM Eastern is 8:59 PM pacific
        val elevenFiftyNineEastern = Instant.ofEpochSecond(1725249540)
        // So this time is 2m ago, not yesterday
        val now = elevenFiftyNineEastern.plus(2, ChronoUnit.MINUTES)

        val pacificTime = ZoneId.of("America/Los_Angeles")
        every { userPreferencesService.getTimeZone(1L) } returns pacificTime

        assertThat(service.displayTime(elevenFiftyNineEastern, 1L, now)).isEqualTo("2m ago")
    }

    @Test
    fun `displayTime formats yesterday time with user's time zone`() {
        val yesterdayNoonPacific = Instant.ofEpochSecond(1725217200)
        val todayNoonPacific = yesterdayNoonPacific.plus(1, ChronoUnit.DAYS)

        val pacificTime = ZoneId.of("America/Los_Angeles")
        every { userPreferencesService.getTimeZone(1L) } returns pacificTime
        assertThat(
            service.displayTime(
                yesterdayNoonPacific,
                1L,
                now = todayNoonPacific,
            )
        ).isEqualTo("Yesterday at 12:00PM PT")
    }

    @Test
    fun `displayTime format longDisplayTime with user's time zone`() {
        val augustFirst = Instant.ofEpochSecond(1722528000)
        val now = augustFirst.plus(30, ChronoUnit.DAYS)
        val pacificTime = ZoneId.of("America/Los_Angeles")
        every { userPreferencesService.getTimeZone(1L) } returns pacificTime

        assertThat(service.displayTime(augustFirst, 1L, now = now)).isEqualTo("Thu Aug 1 at 9:00AM PT")
    }
}
