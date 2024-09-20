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

    @Test
    fun `userChangePassword checks if new passwords match`() {
        val result = userService.userChangePassword(
            user = fixtures.zach,
            currentPassword = "hashedPassword",
            newPassword = "newPassword",
            repeatNewPassword = "differentNewPassword"
        )

        assertThat(result).isInstanceOf(ChangePasswordFailure::class.java)
        val failure = result as ChangePasswordFailure
        assertThat(failure.errorMessage).isEqualTo("Passwords do not match")
    }

    @Test
    fun `userChangePassword checks if current password matches hash`() {
        val result = userService.userChangePassword(
            user = fixtures.zach,
            currentPassword = "wrongPassword",
            newPassword = "newPassword",
            repeatNewPassword = "newPassword"
        )

        assertThat(result).isInstanceOf(ChangePasswordFailure::class.java)
        val failure = result as ChangePasswordFailure
        assertThat(failure.errorMessage).isEqualTo("Current password is incorrect")
    }

    @Test
    fun `userChangePassword changes password if all checks pass`() {
        val result = userService.userChangePassword(
            user = fixtures.zach,
            currentPassword = fixtures.zachPassword,
            newPassword = "newPassword",
            repeatNewPassword = "newPassword"
        )

        assertThat(result).isEqualTo(ChangePasswordSuccess)
        val user = userService.checkUser("zach", "newPassword")
        assertThat(user).isNotNull()
    }

    @Test
    fun `setPassword unconditionally resets user's password`() {
        val user = fixtures.zach
        userService.setPassword(user, "newPassword")
        val updatedUser = userService.checkUser("zach", "newPassword")
        assertThat(updatedUser).isNotNull()
    }
}
