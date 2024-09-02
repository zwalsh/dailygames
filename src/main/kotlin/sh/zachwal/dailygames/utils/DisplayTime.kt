package sh.zachwal.dailygames.utils

import java.text.SimpleDateFormat
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.TextStyle
import java.util.Date
import java.util.Locale
import java.util.TimeZone

val defaultTimeZone = ZoneId.of("America/New_York")

fun displayTime(time: Instant, now: Instant = Instant.now(), userTimeZone: ZoneId = defaultTimeZone): String {
    val nowDate = LocalDate.ofInstant(now, userTimeZone)

    val date = LocalDate.ofInstant(time, userTimeZone)
    val diff = now.epochSecond - time.epochSecond

    return if (date.equals(nowDate)) {
        when (diff) {
            in 0..59 -> "Just now"
            in 60..3599 -> "${diff / 60}m ago"
            in 3600..86399 -> "${diff / 3600}h ${(diff % 3600) / 60}m ago"
            else -> throw IllegalArgumentException("Time difference is too large")
        }
    } else if (date.equals(nowDate.minusDays(1))) {
        val shortTimeZone = userTimeZone.getDisplayName(TextStyle.SHORT, Locale.US)

        val format = SimpleDateFormat("'Yesterday at' h:mma '$shortTimeZone'")
        format.timeZone = TimeZone.getTimeZone(userTimeZone)
        format.format(Date.from(time))
    } else {
        longDisplayTime(time, userTimeZone)
    }
}

fun longDisplayTime(time: Instant, userTimeZone: ZoneId = defaultTimeZone): String {
    val shortTimeZone = userTimeZone.getDisplayName(TextStyle.SHORT, Locale.US)

    val format = SimpleDateFormat("EEE MMM d 'at' h:mma '$shortTimeZone'")
    format.timeZone = TimeZone.getTimeZone(userTimeZone)
    return format.format(Date.from(time))
}
