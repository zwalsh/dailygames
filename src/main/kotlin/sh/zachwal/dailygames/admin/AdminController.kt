package sh.zachwal.dailygames.admin

import io.ktor.application.call
import io.ktor.html.respondHtml
import io.ktor.routing.Routing
import io.ktor.routing.get
import kotlinx.html.a
import kotlinx.html.body
import kotlinx.html.div
import kotlinx.html.h1
import kotlinx.html.h2
import kotlinx.html.head
import kotlinx.html.li
import kotlinx.html.table
import kotlinx.html.tbody
import kotlinx.html.td
import kotlinx.html.th
import kotlinx.html.thead
import kotlinx.html.title
import kotlinx.html.tr
import kotlinx.html.ul
import org.slf4j.LoggerFactory
import sh.zachwal.dailygames.controller.Controller
import sh.zachwal.dailygames.db.jdbi.User
import sh.zachwal.dailygames.roles.Role
import sh.zachwal.dailygames.roles.Role.ADMIN
import sh.zachwal.dailygames.roles.Role.USER
import sh.zachwal.dailygames.roles.RoleService
import sh.zachwal.dailygames.roles.adminRoute
import sh.zachwal.dailygames.shared_html.headSetup
import sh.zachwal.dailygames.users.UserService
import javax.inject.Inject

@Controller
class AdminController @Inject constructor(
    private val userService: UserService,
    private val roleService: RoleService,
) {

    private val logger = LoggerFactory.getLogger(AdminController::class.java)

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
                call.respondHtml {
                    head {
                        title {
                            +"Admin"
                        }
                        headSetup()
                    }
                    body {
                        div(classes = "container") {
                            h1 {
                                +"Admin"
                            }
                            h2 {
                                +"Users"
                            }
                            ul {
                                li {
                                    a(href = "/admin/users") {
                                        +"Users"
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    internal fun Routing.listUsers() {
        adminRoute("/admin/users") {
            get {
                val roles = roleService.allRoles()
                val users = sortedUsers(userService.list(), roles)

                call.respondHtml {
                    head {
                        title { +"Users" }
                        headSetup()
                    }
                    body {
                        div(classes = "container") {
                            h1 { +"Users" }
                            table(classes = "table") {
                                thead {
                                    tr {
                                        th {
                                            +"Name"
                                        }
                                        th {
                                            +"Admin"
                                        }
                                    }
                                }
                                tbody {
                                    users.forEach {
                                        tr {
                                            td {
                                                +it.username
                                            }
                                            td {
                                                if (roles[it]?.contains(USER) == true) {
                                                    +"✅"
                                                } else {
                                                    +"❌"
                                                }
                                            }
                                            td {
                                                if (roles[it]?.contains(ADMIN) == true) {
                                                    +"✅"
                                                } else {
                                                    +"❌"
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
