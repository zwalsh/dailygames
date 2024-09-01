package sh.zachwal.dailygames.users

import sh.zachwal.dailygames.db.dao.UserPreferencesDAO
import java.time.ZoneId
import javax.inject.Singleton

@Singleton
class UserPreferencesService constructor(
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
}