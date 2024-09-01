package sh.zachwal.dailygames.db.dao

import com.google.common.truth.Truth.assertThat
import org.jdbi.v3.core.Jdbi
import org.jdbi.v3.sqlobject.kotlin.onDemand
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import sh.zachwal.dailygames.db.extension.DatabaseExtension
import sh.zachwal.dailygames.db.extension.Fixtures

@ExtendWith(DatabaseExtension::class)
class UserPreferencesDAOTest(
    jdbi: Jdbi,
    private val fixtures: Fixtures,
) {

    private val userPreferencesDAO: UserPreferencesDAO = jdbi.onDemand()

    @Test
    fun `can insert user preferences`() {
        val userPreferences = userPreferencesDAO.createUserPreferencesWithDefaults(fixtures.zach.id)

        assertThat(userPreferences.userId).isEqualTo(fixtures.zach.id)
        assertThat(userPreferences.timeZone).isEqualTo("America/New_York")
    }

    @Test
    fun `cannot insert two user preferences`() {
        userPreferencesDAO.createUserPreferencesWithDefaults(fixtures.zach.id)

        val exception = assertThrows<Exception> {
            userPreferencesDAO.createUserPreferencesWithDefaults(fixtures.zach.id)
        }

        assertThat(exception.message).contains("duplicate key value violates unique constraint")
    }
}