package sh.zachwal.dailygames.home

import org.jdbi.v3.core.Jdbi
import org.jdbi.v3.sqlobject.kotlin.attach
import sh.zachwal.dailygames.db.dao.game.PuzzleResultDAO
import sh.zachwal.dailygames.users.UserPreferencesService
import java.time.Clock
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StreakService @Inject constructor(
    private val jdbi: Jdbi,
    private val userPreferencesService: UserPreferencesService,
    private val clock: Clock,
) {

    /**
     * Returns the user's Daily Games streak overall (for submitting any game).
     */
    fun streakForUser(userId: Long): Int = jdbi.open().use { handle ->
        val timeZone = userPreferencesService.getTimeZoneCached(userId)

        val resultDAO = handle.attach<PuzzleResultDAO>()
        val today = clock.instant()
            .atZone(timeZone)
            .toLocalDate()

        var streak = 0
        var currentDay: LocalDate? = null

        resultDAO.resultsForUserSortedStream(userId)
            .map { it.instantSubmitted.atZone(timeZone).toLocalDate() }
            .takeWhile { date ->
                if (currentDay == null) {
                    if (date == today || date == today.minusDays(1)) {
                        streak++
                        currentDay = date
                        return@takeWhile true
                    }
                    return@takeWhile false
                }
                if (date == currentDay) {
                    return@takeWhile true
                }
                if (date == currentDay!!.minusDays(1)) {
                    streak++
                    currentDay = date
                    return@takeWhile true
                }

                false
            }
            .count()

        return streak
    }
}
