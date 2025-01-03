package sh.zachwal.dailygames.home

import io.ktor.application.call
import io.ktor.html.respondHtml
import io.ktor.http.HttpStatusCode
import io.ktor.request.receiveParameters
import io.ktor.response.respondRedirect
import io.ktor.routing.Routing
import io.ktor.routing.get
import io.ktor.routing.post
import io.ktor.util.getOrFail
import sh.zachwal.dailygames.auth.currentUser
import sh.zachwal.dailygames.auth.userOrNull
import sh.zachwal.dailygames.controller.Controller
import sh.zachwal.dailygames.home.views.HeroView
import sh.zachwal.dailygames.home.views.SHARE_TEXT_ID
import sh.zachwal.dailygames.results.ResultService
import sh.zachwal.dailygames.roles.approvedUserRoute
import sh.zachwal.dailygames.users.UserService
import javax.inject.Inject
import javax.inject.Singleton

@Controller
@Singleton
class HomeController @Inject constructor(
    private val userService: UserService,
    private val homeService: HomeService,
    private val resultService: ResultService,
) {

    internal fun Routing.home() {
        get("/") {
            val user = userOrNull(call, userService)
            val view = user?.let { homeService.homeView(user) } ?: HeroView

            call.respondHtml(HttpStatusCode.OK) {
                view.renderIn(this)
            }
        }
    }

    internal fun Routing.postResult() {
        approvedUserRoute("/") {
            post {
                val user = currentUser(call, userService)
                val params = call.receiveParameters()
                val shareText = params.getOrFail(SHARE_TEXT_ID)

                resultService.createResult(user, shareText)

                call.respondRedirect("/?showModal=true")
            }
        }
    }
}
