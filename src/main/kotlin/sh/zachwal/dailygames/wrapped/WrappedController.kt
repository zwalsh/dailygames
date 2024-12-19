package sh.zachwal.dailygames.wrapped

import io.ktor.application.call
import io.ktor.html.respondHtml
import io.ktor.response.respond
import io.ktor.routing.Routing
import io.ktor.routing.get
import io.ktor.util.getOrFail
import sh.zachwal.dailygames.controller.Controller
import sh.zachwal.dailygames.roles.adminRoute
import sh.zachwal.dailygames.roles.approvedUserRoute
import javax.inject.Inject

@Controller
class WrappedController @Inject constructor(
    private val wrappedService: WrappedService,
) {

    internal fun Routing.wrapped() {
        approvedUserRoute("/wrapped/{year}/{wrappedId}") {
            get {
                val year = call.parameters.getOrFail("year").toInt()
                val wrappedId = call.parameters.getOrFail("wrappedId")

                call.respondHtml {
                    wrappedService.wrappedView(year, wrappedId).renderIn(this)
                }
            }
        }
    }

    internal fun Routing.wrappedTest() {
        adminRoute("/admin/wrapped-test") {
            get {
                call.respond(wrappedService.generateWrappedData())
            }
        }
    }
}
