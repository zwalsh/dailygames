package sh.zachwal.dailygames.utils

import java.text.SimpleDateFormat
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.util.Date

fun displayTime(time: Instant, now: Instant = Instant.now()): String {
    val nowDate = LocalDate.ofInstant(now, ZoneId.of("America/New_York"))

    val date = LocalDate.ofInstant(time, ZoneId.of("America/New_York"))
    val diff = now.epochSecond - time.epochSecond

    if (date.equals(nowDate)) {
        return when (diff) {
            in 0..59 -> "Just now"
            in 60..3599 -> "${diff / 60}m ago"
            in 3600..86399 -> "${diff / 3600}h${(diff % 3600) / 60}m ago"
            else -> throw IllegalArgumentException("Time difference is too large")
        }
    } else if (date.equals(nowDate.minusDays(1))) {
        return SimpleDateFormat("'Yesterday at' h:mma 'ET'").format(Date.from(time))
    } else {
        return longDisplayTime(time)
    }
}

fun longDisplayTime(time: Instant): String = SimpleDateFormat("EEE MMM d 'at' h:mma 'ET'").format(Date.from(time))
