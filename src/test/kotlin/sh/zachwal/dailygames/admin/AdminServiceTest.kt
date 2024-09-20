package sh.zachwal.dailygames.admin

import com.google.common.truth.Truth.assertThat
import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Test
import sh.zachwal.dailygames.db.jdbi.User
import sh.zachwal.dailygames.roles.Role
import sh.zachwal.dailygames.roles.RoleService
import sh.zachwal.dailygames.users.UserService

class AdminServiceTest {
    private val userService: UserService = mockk {
        every { setPassword(any(), any()) } just Runs
    }
    private val roleService: RoleService = mockk()

    private val service = AdminService(userService, roleService)

    @Test
    fun `resetUserPassword returns error message when user not found`() {
        every { userService.getUser("testy") } returns null

        val result = service.resetUserPassword("testy", "testy")

        assertThat(result.errorMessage).isEqualTo("User testy not found")
    }

    @Test
    fun `resetUserPassword returns error message when user is admin`() {
        every { userService.getUser("admin") } returns User(1L, "admin", "admin")
        every { roleService.hasRole(any(), Role.ADMIN) } returns true

        val result = service.resetUserPassword("admin", "newPassword")

        assertThat(result.errorMessage).isEqualTo("Cannot reset password for admin user admin")
    }

    @Test
    fun `resetUserPassword sets new password for user`() {
        val testUser = User(1L, "testy", "testy")
        every { userService.getUser("testy") } returns testUser
        every { roleService.hasRole(any(), Role.ADMIN) } returns false

        val result = service.resetUserPassword("testy", "newPassword")

        assertThat(result.successMessage).isEqualTo("Password reset for user testy")
        verify { userService.setPassword(testUser, "newPassword") }
    }
}
