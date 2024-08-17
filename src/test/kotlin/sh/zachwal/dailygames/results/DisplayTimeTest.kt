package sh.zachwal.dailygames.results

import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.Test
import java.time.Instant
import java.time.temporal.ChronoUnit

class DisplayTimeTest {

    @Test
    fun `displayTime returns just now for recent results`() {
        val justNow = Instant.now()

        assertThat(displayTime(justNow)).isEqualTo("Just now")
    }

    @Test
    fun `displayTime returns 1 minute ago for results 1 minute ago`() {
        val oneMinuteAgo = Instant.now().minusSeconds(60)

        assertThat(displayTime(oneMinuteAgo)).isEqualTo("1m ago")
    }

    @Test
    fun `displayTime returns 30min ago for results 30min ago`() {
        val oneHourAgo = Instant.now().minusSeconds(60 * 30)

        assertThat(displayTime(oneHourAgo)).isEqualTo("30m ago")
    }

    @Test
    fun `displayTime returns 1h ago for results 1h ago`() {
        val oneHourAgo = Instant.now().minusSeconds(60 * 60)

        assertThat(displayTime(oneHourAgo)).isEqualTo("1h0m ago")
    }

    @Test
    fun `displayTime returns combination of hours and minutes`() {
        val time = Instant.now().minusSeconds(60 * 90)

        assertThat(displayTime(time)).isEqualTo("1h30m ago")
    }

    @Test
    fun `displayTime returns hours and minutes for up to 24 hours when same day`() {
        val midnightAug17 = Instant.ofEpochSecond(1723867200)
        val eleven59PMAug17 = Instant.ofEpochSecond(1723953540)

        assertThat(displayTime(midnightAug17, now = eleven59PMAug17)).isEqualTo("23h59m ago")
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
}
