package sh.zachwal.dailygames.leaderboard

import io.ktor.application.call
import io.ktor.html.respondHtml
import io.ktor.http.HttpStatusCode
import io.ktor.routing.Routing
import io.ktor.routing.get
import sh.zachwal.dailygames.auth.currentUser
import sh.zachwal.dailygames.controller.Controller
import sh.zachwal.dailygames.roles.approvedUserRoute
import sh.zachwal.dailygames.users.UserService
import javax.inject.Inject
import javax.inject.Singleton

@Controller
@Singleton
class LeaderboardController @Inject constructor(
    private val userService: UserService,
    private val leaderboardService: LeaderboardService,
) {

    fun Routing.leaderboard() {
        approvedUserRoute("/leaderboard") {
            get {
                val currentUser = currentUser(call, userService)
                val view = leaderboardService.leaderboardView(currentUser)

                call.respondHtml(HttpStatusCode.OK) {
                    view.renderIn(this)
                }
            }
        }
    }
}
