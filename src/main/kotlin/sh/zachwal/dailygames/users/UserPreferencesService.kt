package sh.zachwal.dailygames.users

import sh.zachwal.dailygames.db.dao.UserPreferencesDAO
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.TextStyle
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserPreferencesService @Inject constructor(
    private val userPreferencesDAO: UserPreferencesDAO
) {

    fun getTimeZone(userId: Long): ZoneId {
        return userPreferencesDAO.getByUserId(userId)?.let {
            ZoneId.of(it.timeZone)
        } ?: ZoneId.of("America/New_York")
    }

    fun setTimeZone(userId: Long, zoneId: ZoneId) {
        userPreferencesDAO.updateTimeZone(userId, zoneId.id)
    }

    val popularTimeZones = listOf(
        ZoneId.of("America/New_York"),
        ZoneId.of("America/Chicago"),
        ZoneId.of("America/Denver"),
        ZoneId.of("America/Phoenix"),
        ZoneId.of("America/Los_Angeles"),
    ).associateWith { it.displayString() }

    private fun ZoneId.displayString(): String {
        val zonedDateTime = ZonedDateTime.now(this)
        val offset = zonedDateTime.offset
        val city = this.id.split("/").lastOrNull()?.replace("_", " ")
        return "(GMT${offset.id}) ${this.getDisplayName(TextStyle.FULL, Locale.US)}" +
            if (city != null) " - $city" else ""
    }

    private fun ZoneId.gmtOffset(): String {
        val zonedDateTime = ZonedDateTime.now(this)
        val offset = zonedDateTime.offset
        return "(GMT${offset.id})"
    }

    val possibleTimeZones: Map<ZoneId, String> by lazy {
        popularTimeZones +
            ZoneId.getAvailableZoneIds()
                .map { ZoneId.of(it) }
                .filter { !popularTimeZones.containsKey(it) }
                .sortedBy { it.gmtOffset() }
                .associateWith { it.displayString() }
    }
}
