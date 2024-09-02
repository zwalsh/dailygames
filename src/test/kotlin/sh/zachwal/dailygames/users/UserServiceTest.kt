package sh.zachwal.dailygames.users

import com.google.common.truth.Truth.assertThat
import org.jdbi.v3.core.Jdbi
import org.jdbi.v3.sqlobject.kotlin.onDemand
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import sh.zachwal.dailygames.db.dao.UserDAO
import sh.zachwal.dailygames.db.dao.UserPreferencesDAO
import sh.zachwal.dailygames.db.extension.DatabaseExtension
import sh.zachwal.dailygames.db.extension.Fixtures

@ExtendWith(DatabaseExtension::class)
class UserServiceTest(
    val jdbi: Jdbi,
    val fixtures: Fixtures,
) {
    private val userDAO: UserDAO = jdbi.onDemand()
    private val userPreferencesDAO: UserPreferencesDAO = jdbi.onDemand()
    private val userService = UserService(
        userDAO = userDAO,
        userPreferencesDAO = userPreferencesDAO,
    )

    @Test
    fun `creating user creates default preferences`() {
        val user = userService.createUser("testy", "testy")!!
        val userPreferences = userPreferencesDAO.getByUserId(user.id)
        assertThat(userPreferences).isNotNull()
    }
}
