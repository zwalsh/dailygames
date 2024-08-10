package sh.zachwal.dailygames.home

import io.ktor.application.call
import io.ktor.html.respondHtml
import io.ktor.http.HttpStatusCode
import io.ktor.routing.Routing
import io.ktor.routing.get
import org.slf4j.LoggerFactory
import sh.zachwal.dailygames.auth.userOrNull
import sh.zachwal.dailygames.controller.Controller
import sh.zachwal.dailygames.home.views.HeroView
import sh.zachwal.dailygames.users.UserService
import javax.inject.Inject
import javax.inject.Singleton

@Controller
@Singleton
class HomeController @Inject constructor(
    private val userService: UserService,
    private val homeService: HomeService,
) {

    private val logger = LoggerFactory.getLogger(HomeController::class.java)

    internal fun Routing.home() {
        get("/") {
            logger.info("Hit home page.")
            val user = userOrNull(call, userService)
            val view = user?.let { homeService.homeView(user) } ?: HeroView

            call.respondHtml(HttpStatusCode.OK) {
                view.renderIn(this)
            }
        }
    }
}
