package sh.zachwal.dailygames.utils

import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.Test
import java.time.Instant
import java.time.ZoneId
import java.time.temporal.ChronoUnit

class DisplayTimeTest {

    @Test
    fun `displayTime returns just now for recent results`() {
        val now = Instant.ofEpochSecond(1724074018)
        val justNow = now.minusSeconds(1)

        assertThat(displayTime(justNow, now = now)).isEqualTo("Just now")
    }

    @Test
    fun `displayTime returns 1 minute ago for results 1 minute ago`() {
        val now = Instant.ofEpochSecond(1724074018)
        val oneMinuteAgo = now.minusSeconds(60)

        assertThat(displayTime(oneMinuteAgo, now = now)).isEqualTo("1m ago")
    }

    @Test
    fun `displayTime returns 30min ago for results 30min ago`() {
        val now = Instant.ofEpochSecond(1724074018)
        val thirtyMinutesAgo = now.minusSeconds(60 * 30)

        assertThat(displayTime(thirtyMinutesAgo, now = now)).isEqualTo("30m ago")
    }

    @Test
    fun `displayTime returns 1h ago for results 1h ago`() {
        val now = Instant.ofEpochSecond(1724074018)
        val oneHourAgo = now.minusSeconds(60 * 60)

        assertThat(displayTime(oneHourAgo, now = now)).isEqualTo("1h 0m ago")
    }

    @Test
    fun `displayTime returns combination of hours and minutes`() {
        val time = Instant.now().minusSeconds(60 * 90)

        assertThat(displayTime(time)).isEqualTo("1h 30m ago")
    }

    @Test
    fun `displayTime returns hours and minutes for up to 24 hours when same day`() {
        val midnightAug17 = Instant.ofEpochSecond(1723867200)
        val eleven59PMAug17 = Instant.ofEpochSecond(1723953540)

        assertThat(displayTime(midnightAug17, now = eleven59PMAug17)).isEqualTo("23h 59m ago")
    }

    @Test
    fun `displayTime returns yesterday when result is from yesterday`() {
        // These times are one minute apart
        val midnightAug17 = Instant.ofEpochSecond(1723867200)
        val eleven59PMAug16 = Instant.ofEpochSecond(1723867140)

        assertThat(displayTime(eleven59PMAug16, now = midnightAug17)).isEqualTo("Yesterday at 11:59PM ET")
    }

    @Test
    fun `displayTime returns yesterday when result is from yesterday and time is midnight`() {
        val midnightAug17 = Instant.ofEpochSecond(1723867200)
        val midnightAug16 = Instant.ofEpochSecond(1723780800)

        assertThat(displayTime(midnightAug16, now = midnightAug17)).isEqualTo("Yesterday at 12:00AM ET")
    }

    @Test
    fun `displayTime formats with day of week, month, day of month, time when from before yesterday`() {
        val midnightAug17 = Instant.ofEpochSecond(1723867200)
        val now = midnightAug17.plus(2, ChronoUnit.DAYS)

        assertThat(displayTime(midnightAug17, now)).isEqualTo("Sat Aug 17 at 12:00AM ET")
    }

    @Test
    fun `displayTime formats with user time zone`() {
        // 11:59PM Eastern is 8:59 PM pacific
        val elevenFiftyNineEastern = Instant.ofEpochSecond(1725249540)
        // So this time is 2m ago, not yesterday
        val now = elevenFiftyNineEastern.plus(2, ChronoUnit.MINUTES)

        val pacificTime = ZoneId.of("America/Los_Angeles")
        assertThat(displayTime(elevenFiftyNineEastern, now, userTimeZone = pacificTime)).isEqualTo("2m ago")
    }

    @Test
    fun `displayTime formats yesterday time with user's time zone`() {
        val yesterdayNoonPacific = Instant.ofEpochSecond(1725217200)
        val todayNoonPacific = yesterdayNoonPacific.plus(1, ChronoUnit.DAYS)

        val pacificTime = ZoneId.of("America/Los_Angeles")
        assertThat(
            displayTime(
                yesterdayNoonPacific,
                todayNoonPacific,
                userTimeZone = pacificTime
            )
        ).isEqualTo("Yesterday at 12:00PM PT")
    }

    @Test
    fun `displayTime format longDisplayTime with user's time zone`() {
        val augustFirst = Instant.ofEpochSecond(1722528000)
        val now = augustFirst.plus(30, ChronoUnit.DAYS)
        val pacificTime = ZoneId.of("America/Los_Angeles")

        assertThat(
            displayTime(
                augustFirst,
                now = now,
                userTimeZone = pacificTime
            )
        ).isEqualTo("Thu Aug 1 at 9:00AM PT")
    }
}
