package sh.zachwal.dailygames.wrapped

import io.ktor.application.call
import io.ktor.html.respondHtml
import io.ktor.response.respond
import io.ktor.routing.Routing
import io.ktor.routing.get
import io.ktor.util.getOrFail
import sh.zachwal.dailygames.auth.currentUser
import sh.zachwal.dailygames.controller.Controller
import sh.zachwal.dailygames.roles.adminRoute
import sh.zachwal.dailygames.roles.approvedUserRoute
import sh.zachwal.dailygames.users.UserService
import javax.inject.Inject

@Controller
class WrappedController @Inject constructor(
    private val wrappedService: WrappedService,
    private val userService: UserService,
) {

    internal fun Routing.wrapped() {

        adminRoute("/wrapped/{year}/as/{userName}") {
            get {
                val year = call.parameters.getOrFail("year").toInt()
                val username = call.parameters.getOrFail("userName")
                val user = userService.getUser(username) ?: throw RuntimeException("User=$username not found")
                val wrappedView = wrappedService.wrappedView(year, user)

                call.respondHtml {
                    wrappedView.renderIn(this)
                }
            }
        }

        approvedUserRoute("/wrapped/{year}") {
            get {
                val year = call.parameters.getOrFail("year").toInt()
                val user = currentUser(call, userService)
                val wrappedView = wrappedService.wrappedView(year, user)

                call.respondHtml {
                    wrappedView.renderIn(this)
                }
            }
        }

        // Open so that link previews look nice in iOS
        // TODO -- have login redirects include destination page & meta tags for that page
        // see https://developer.apple.com/library/archive/technotes/tn2444/_index.html
        get("/wrapped/{year}/{userName}") {
            val year = call.parameters.getOrFail("year").toInt()
            val username = call.parameters.getOrFail("userName")
            val wrappedView = wrappedService.guestWrappedView(year, username)

            call.respondHtml {
                wrappedView.renderIn(this)
            }
        }
    }

    internal fun Routing.wrappedTest() {
        adminRoute("/admin/wrapped-test") {
            get {
                call.respond(wrappedService.generateWrappedData(year = 2024)) // TODO set year
            }
        }
    }
}
