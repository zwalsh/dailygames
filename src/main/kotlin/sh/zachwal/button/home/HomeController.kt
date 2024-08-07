package sh.zachwal.button.home

import io.ktor.application.call
import io.ktor.html.respondHtml
import io.ktor.http.HttpStatusCode
import io.ktor.routing.Routing
import io.ktor.routing.get
import kotlinx.html.body
import kotlinx.html.div
import kotlinx.html.h1
import kotlinx.html.head
import kotlinx.html.title
import org.slf4j.LoggerFactory
import sh.zachwal.button.controller.Controller
import sh.zachwal.button.shared_html.favicon
import sh.zachwal.button.shared_html.mobileUI
import sh.zachwal.button.shared_html.sentryScript
import javax.inject.Inject

const val TOKEN_PARAMETER = "t"

@Controller
class HomeController @Inject constructor() {

    private val logger = LoggerFactory.getLogger(HomeController::class.java)

    internal fun Routing.home() {
        get("/") {
            logger.info("Hit home page.")
            call.respondHtml(HttpStatusCode.OK) {
                head {
                    title("Daily Games")
                    mobileUI()
                    favicon()
                    sentryScript()
                }
                body {
                    div(classes = "container") {
                        h1 {
                            +"Hello, world!"
                        }
                    }
                }
            }
        }
    }
}
