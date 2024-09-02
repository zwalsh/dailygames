package sh.zachwal.dailygames.users

import com.google.common.truth.Truth.assertThat
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Test
import sh.zachwal.dailygames.db.dao.UserPreferencesDAO
import sh.zachwal.dailygames.db.jdbi.UserPreferences
import java.time.ZoneId

class UserPreferencesServiceTest {

    private val userPreferencesDAO: UserPreferencesDAO = mockk(relaxed = true)
    private val userPreferencesService = UserPreferencesService(userPreferencesDAO)

    @Test
    fun `getTimeZone parses timezone from user preferences`() {
        every { userPreferencesDAO.getByUserId(1) } returns UserPreferences(1, 1, "America/Los_Angeles")
        val timeZone = userPreferencesService.getTimeZone(1)

        assertThat(timeZone).isEqualTo(ZoneId.of("America/Los_Angeles"))
    }

    @Test
    fun `getTimeZone returns default timezone when user has no preferences`() {
        every { userPreferencesDAO.getByUserId(1) } returns null
        val timeZone = userPreferencesService.getTimeZone(1)

        assertThat(timeZone).isEqualTo(ZoneId.of("America/New_York"))
    }

    @Test
    fun `can set time zone`() {
        userPreferencesService.setTimeZone(1, ZoneId.of("America/Los_Angeles"))
        verify {
            userPreferencesDAO.updateTimeZone(1, "America/Los_Angeles")
        }
    }

    @Test
    fun `possibleTimeZones lists most common ones first`() {
        val possibleTimeZones = userPreferencesService.possibleTimeZones
        val commonTimeZones = listOf(
            ZoneId.of("America/New_York"),
            ZoneId.of("America/Chicago"),
            ZoneId.of("America/Denver"),
            ZoneId.of("America/Phoenix"),
            ZoneId.of("America/Los_Angeles"),
        )

        assertThat(possibleTimeZones.keys.take(5)).containsExactlyElementsIn(commonTimeZones)
    }

    @Test
    fun `possibleTimeZones includes friendly display names`() {
        val possibleTimeZones = userPreferencesService.possibleTimeZones

        assertThat(possibleTimeZones[ZoneId.of("America/New_York")]).contains("Eastern Time - New York")
    }
}
