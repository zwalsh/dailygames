package sh.zachwal.dailygames.admin

import io.ktor.application.call
import io.ktor.html.respondHtml
import io.ktor.request.receiveParameters
import io.ktor.routing.Routing
import io.ktor.routing.get
import io.ktor.routing.post
import io.ktor.util.getOrFail
import sh.zachwal.dailygames.admin.views.AdminPageView
import sh.zachwal.dailygames.admin.views.ResetUserPasswordView
import sh.zachwal.dailygames.admin.views.UserListView
import sh.zachwal.dailygames.admin.views.UserRowView
import sh.zachwal.dailygames.auth.currentUser
import sh.zachwal.dailygames.controller.Controller
import sh.zachwal.dailygames.db.jdbi.User
import sh.zachwal.dailygames.nav.NavItem
import sh.zachwal.dailygames.nav.NavViewFactory
import sh.zachwal.dailygames.roles.Role
import sh.zachwal.dailygames.roles.Role.ADMIN
import sh.zachwal.dailygames.roles.Role.USER
import sh.zachwal.dailygames.roles.RoleService
import sh.zachwal.dailygames.roles.adminRoute
import sh.zachwal.dailygames.users.UserService
import javax.inject.Inject
import sh.zachwal.dailygames.admin.views.AdminStreakPageView
import sh.zachwal.dailygames.home.StreakService

const val USERNAME_FORM_PARAM = "username"
const val NEW_PASSWORD_FORM_PARAM = "newPassword"

@Controller
class AdminController @Inject constructor(
    private val userService: UserService,
    private val roleService: RoleService,
    private val adminService: AdminService,
    private val navViewFactory: NavViewFactory,
    private val streakService: StreakService,
) {

    private fun sortedUsers(users: List<User>, roles: Map<User, List<Role>>): List<User> {
        return users.sortedWith(
            Comparator { u1, u2 ->
                val u1Roles = roles[u1]
                val u2Roles = roles[u2]

                val u1Admin = u1Roles?.contains(ADMIN) ?: false
                val u2Admin = u2Roles?.contains(ADMIN) ?: false

                if (u1Admin && !u2Admin) {
                    return@Comparator -1
                }
                if (u2Admin && !u1Admin) {
                    return@Comparator 1
                }

                val u1User = u1Roles?.contains(USER) ?: false
                val u2User = u2Roles?.contains(USER) ?: false

                if (u1User && !u2User) {
                    return@Comparator -1
                }
                if (u2User && !u1User) {
                    return@Comparator 1
                }

                u1.username.lowercase().compareTo(u2.username.lowercase())
            }
        )
    }

    internal fun Routing.admin() {
        adminRoute("/admin") {
            get {
                val currentUser = currentUser(call, userService)
                val navView = navViewFactory.navView(currentUser.username, NavItem.PROFILE)
                val adminPageView = AdminPageView(navView)
                call.respondHtml {
                    adminPageView.renderIn(this)
                }
            }
        }
    }

    internal fun Routing.listUsers() {
        adminRoute("/admin/users") {
            get {
                val roles = roleService.allRoles()
                val users = sortedUsers(userService.list(), roles)

                val userRowViews = users.map {
                    UserRowView(
                        username = it.username,
                        isAdmin = roles[it]?.contains(ADMIN) == true
                    )
                }
                val currentUser = currentUser(call, userService)
                val navView = navViewFactory.navView(currentUser.username, NavItem.PROFILE)
                val userListView = UserListView(
                    users = userRowViews,
                    navView = navView,
                )

                call.respondHtml {
                    userListView.renderIn(this)
                }
            }
        }
    }

    internal fun Routing.resetPassword() {
        adminRoute("/admin/reset-password") {
            get {
                val view = ResetUserPasswordView()
                call.respondHtml {
                    view.renderIn(this)
                }
            }
            post {
                val params = call.receiveParameters()
                val username = params.getOrFail(USERNAME_FORM_PARAM)
                val newPassword = params.getOrFail(NEW_PASSWORD_FORM_PARAM)

                val view = adminService.resetUserPassword(username, newPassword)
                call.respondHtml {
                    view.renderIn(this)
                }
            }
        }
    }

    internal fun Routing.streaks() {
        adminRoute("/admin/streaks") {
            get {
                val userStreaks = userService.list().associateWith {
                    streakService.streakForUser(it.id)
                }
                val currentUser = currentUser(call, userService)
                val navView = navViewFactory.navView(currentUser.username, NavItem.PROFILE)
                val view = AdminStreakPageView(userStreaks, navView)
                call.respondHtml {
                    view.renderIn(this)
                }
            }
        }
    }
}
