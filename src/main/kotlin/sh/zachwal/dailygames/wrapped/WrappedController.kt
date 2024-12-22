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
        adminRoute("/wrapped/{year}") {
            get {
                val year = call.parameters.getOrFail("year").toInt()
                val userId = currentUser(call, userService).id

                call.respondHtml {
                    wrappedService.wrappedView(year, userId).renderIn(this)
                }
            }
        }

        adminRoute("/wrapped/{year}/{userId}") {
            get {
                val year = call.parameters.getOrFail("year").toInt()
                val userId = call.parameters.getOrFail("userId").toLong()

                call.respondHtml {
                    wrappedService.wrappedView(year, userId).renderIn(this)
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
