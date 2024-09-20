package sh.zachwal.dailygames.admin

import sh.zachwal.dailygames.admin.views.ResetUserPasswordView
import sh.zachwal.dailygames.roles.Role
import sh.zachwal.dailygames.roles.RoleService
import sh.zachwal.dailygames.users.UserService
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AdminService @Inject constructor(
    private val userService: UserService,
    private val roleService: RoleService,
) {

    fun resetUserPassword(username: String, newPassword: String): ResetUserPasswordView {
        val user = userService.getUser(username) ?: return ResetUserPasswordView(
            errorMessage = "User $username not found",
        )
        if (roleService.hasRole(user, Role.ADMIN)) {
            return ResetUserPasswordView(
                errorMessage = "Cannot reset password for admin user $username",
            )
        }
        userService.setPassword(user, newPassword)
        return ResetUserPasswordView(
            successMessage = "Password reset for user $username",
        )
    }
}
