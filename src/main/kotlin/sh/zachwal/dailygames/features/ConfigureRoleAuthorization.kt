package sh.zachwal.dailygames.features

import io.ktor.application.Application
import io.ktor.application.log
import sh.zachwal.dailygames.roles.RoleAuthorization.RoleBasedAuthorizer
import sh.zachwal.dailygames.roles.RoleService
import sh.zachwal.dailygames.users.UserService

fun configureRoleAuthorization(roleBasedAuthorizer: RoleBasedAuthorizer, application: Application, userService: UserService, roleService: RoleService) {
    roleBasedAuthorizer.validate { roles, session ->
        application.log.debug("Checking {} for session {}", roles, session.user)
        val user = userService.getUser(session.user)
        user?.let { roleService.firstRoleOrNull(user, roles) }
    }
}
