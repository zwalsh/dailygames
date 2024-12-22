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
import sh.zachwal.dailygames.users.UserService
import javax.inject.Inject

@Controller
class WrappedController @Inject constructor(
    private val wrappedService: WrappedService,
    private val userService: UserService,
) {

    internal fun Routing.wrapped() {
        adminRoute("/wrapped/{year}") {
            get {
                val year = call.parameters.getOrFail("year").toInt()
<<<<<<< HEAD
                val userId = currentUser(call, userService).id
                val wrappedView = wrappedService.wrappedView(year, userId)

                call.respondHtml {
                    wrappedView.renderIn(this)
=======
                val user = currentUser(call, userService)

                call.respondHtml {
                    wrappedService.wrappedView(year, user).renderIn(this)
>>>>>>> 583f4fd... Wrapped guest page
                }
            }
        }

        adminRoute("/wrapped/{year}/{userName}") {
            get {
                val year = call.parameters.getOrFail("year").toInt()
<<<<<<< HEAD
                val userId = call.parameters.getOrFail("userId").toLong()
                val wrappedView = wrappedService.wrappedView(year, userId)

                call.respondHtml {
                    wrappedView.renderIn(this)
=======
                val username = call.parameters.getOrFail("userName")

                call.respondHtml {
                    wrappedService.guestWrappedView(year, username).renderIn(this)
>>>>>>> 583f4fd... Wrapped guest page
                }
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
