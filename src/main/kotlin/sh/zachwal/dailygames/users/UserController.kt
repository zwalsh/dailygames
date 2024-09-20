package sh.zachwal.dailygames.users

import io.ktor.application.call
import io.ktor.auth.UserIdPrincipal
import io.ktor.auth.authenticate
import io.ktor.auth.principal
import io.ktor.html.respondHtml
import io.ktor.http.HttpStatusCode
import io.ktor.request.receiveParameters
import io.ktor.response.respond
import io.ktor.response.respondRedirect
import io.ktor.routing.Routing
import io.ktor.routing.get
import io.ktor.routing.post
import io.ktor.routing.route
import io.ktor.sessions.clear
import io.ktor.sessions.get
import io.ktor.sessions.sessions
import io.ktor.util.getOrFail
import sh.zachwal.dailygames.auth.currentUser
import sh.zachwal.dailygames.controller.Controller
import sh.zachwal.dailygames.roles.Role.ADMIN
import sh.zachwal.dailygames.roles.Role.USER
import sh.zachwal.dailygames.roles.RoleService
import sh.zachwal.dailygames.roles.approvedUserRoute
import sh.zachwal.dailygames.session.SessionService
import sh.zachwal.dailygames.session.principals.UserSessionPrincipal
import sh.zachwal.dailygames.users.views.ChangePasswordView
import sh.zachwal.dailygames.users.views.LoginView
import sh.zachwal.dailygames.users.views.ProfileView
import sh.zachwal.dailygames.users.views.RegisterView
import sh.zachwal.dailygames.users.views.TimeZoneFormView
import java.time.ZoneId
import java.time.zone.ZoneRulesException
import javax.inject.Inject

const val POST_TIME_ZONE_ROUTE = "/profile/timezone"
const val TIME_ZONE_FORM_PARAM = "timeZone"

const val CURRENT_PASSWORD_FORM_PARAM = "currentPassword"
const val NEW_PASSWORD_FORM_PARAM = "newPassword"
const val REPEAT_NEW_PASSWORD_FORM_PARAM = "repeatNewPassword"

@Controller
class UserController @Inject constructor(
    private val sessionService: SessionService,
    private val userService: UserService,
    private val roleService: RoleService,
    private val userPreferencesService: UserPreferencesService,
) {
    internal fun Routing.loginRoutes() {
        route("/login") {
            get {
                val session = call.sessions.get<UserSessionPrincipal>()
                if (session?.isValid() == true) {
                    return@get call.respondRedirect("/profile")
                } else if (session?.isValid() == false) {
                    call.sessions.clear<UserSessionPrincipal>()
                }

                val failed = call.request.queryParameters["failed"]?.equals("true") ?: false
                val loginView = LoginView(failed)
                call.respondHtml {
                    loginView.renderIn(this)
                }
            }
            authenticate("form") {
                post {
                    val p = call.principal<UserIdPrincipal>()
                        ?: return@post call.respond(
                            HttpStatusCode.InternalServerError,
                            "No User principal found after post"
                        )
                    sessionService.createUserSession(call, p.name)
                    call.respondRedirect("/")
                }
            }
        }
    }

    private fun greeting(): String = listOf(
        "Hello",
        "Sup",
        "Hi",
        "Howdy",
        "Salutations",
        "What's good"
    ).random()

    internal fun Routing.profileRoute() {
        route("/profile") {
            // not approvedUserRoute because registered (& not "approved") users can see this
            authenticate {
                get {
                    val p = call.sessions.get<UserSessionPrincipal>()
                    if (p == null) {
                        call.respondRedirect("/login")
                        return@get
                    }
                    val user = currentUser(call, userService)
                    val timeZone = userPreferencesService.getTimeZone(user.id)

                    val timeZoneFormView = TimeZoneFormView(
                        currentTimeZone = timeZone,
                        popularTimeZones = userPreferencesService.popularTimeZones.keys.toList(),
                        timeZonesToNames = userPreferencesService.possibleTimeZones,
                    )
                    val profileView = ProfileView(
                        greeting = greeting(),
                        username = user.username,
                        isAdmin = roleService.hasRole(user, ADMIN),
                        timeZoneFormView = timeZoneFormView,
                    )

                    call.respondHtml {
                        profileView.renderIn(this)
                    }
                }
            }
        }
    }

    internal fun Routing.logoutRoute() {
        route("/logout") {
            authenticate {
                get {
                    call.sessions.clear<UserSessionPrincipal>()
                    call.respondRedirect("/login")
                }
            }
        }
    }

    internal fun Routing.registerRoutes() {
        route("/register") {
            get {
                call.respondHtml {
                    RegisterView.renderIn(this)
                }
            }
            post {
                val params = call.receiveParameters()
                val user = userService.createUser(
                    params.getOrFail("username"),
                    params.getOrFail("password")
                )

                if (user != null) {
                    roleService.grantRole(user, USER)
                    sessionService.createUserSession(call, user.username)
                    call.respondRedirect("/")
                } else {
                    call.respond(HttpStatusCode.Conflict, "User already exists")
                }
            }
        }
    }

    internal fun Routing.changePassword() {
        approvedUserRoute("/profile/password") {
            get {
                val view = ChangePasswordView()
                call.respondHtml {
                    view.renderIn(this)
                }
            }

            post {
                val user = currentUser(call, userService)
                val params = call.receiveParameters()
                val currentPassword = params.getOrFail(CURRENT_PASSWORD_FORM_PARAM)
                val newPassword = params.getOrFail(NEW_PASSWORD_FORM_PARAM)
                val repeatNewPassword = params.getOrFail(REPEAT_NEW_PASSWORD_FORM_PARAM)

                val changePasswordResult = userService.userChangePassword(user, currentPassword, newPassword, repeatNewPassword)

                when (changePasswordResult) {
                    is ChangePasswordFailure -> {
                        val view = ChangePasswordView(changePasswordResult.errorMessage)
                        call.respondHtml {
                            view.renderIn(this)
                        }
                    }
                    ChangePasswordSuccess -> {
                        call.respondRedirect("/profile")
                    }
                }
            }
        }
    }

    internal fun Routing.postTimeZone() {
        approvedUserRoute(POST_TIME_ZONE_ROUTE) {
            post {
                val user = currentUser(call, userService)
                val params = call.receiveParameters()
                val timeZone = params.getOrFail(TIME_ZONE_FORM_PARAM)
                val zoneId = try {
                    ZoneId.of(timeZone)
                } catch (e: ZoneRulesException) {
                    call.respond(HttpStatusCode.BadRequest, "Invalid time zone")
                    return@post
                }
                userPreferencesService.setTimeZone(user.id, zoneId)
                call.respondRedirect("/profile")
            }
        }
    }
}
