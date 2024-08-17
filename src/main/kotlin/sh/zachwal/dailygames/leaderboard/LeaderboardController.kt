package sh.zachwal.dailygames.leaderboard

import io.ktor.application.call
import io.ktor.html.respondHtml
import io.ktor.http.HttpStatusCode
import io.ktor.response.respond
import io.ktor.routing.Routing
import io.ktor.routing.get
import sh.zachwal.dailygames.auth.currentUser
import sh.zachwal.dailygames.controller.Controller
import sh.zachwal.dailygames.db.jdbi.puzzle.Game
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
                val view = leaderboardService.overallLeaderboardView(currentUser)

                call.respondHtml(HttpStatusCode.OK) {
                    view.renderIn(this)
                }
            }
        }
    }

    fun Routing.gameLeaderboard() {
        approvedUserRoute("/leaderboard/{game}") {
            get {
                val currentUser = currentUser(call, userService)
                val gameStr = call.parameters["game"]
                if (gameStr == null) {
                    call.respond(HttpStatusCode.NotFound)
                    return@get
                }
                val game = try {
                    Game.valueOf(gameStr.uppercase())
                } catch (e: IllegalArgumentException) {
                    call.respond(HttpStatusCode.NotFound)
                    return@get
                }

                val view = leaderboardService.gameLeaderboardView(currentUser, game)

                call.respondHtml(HttpStatusCode.OK) {
                    view.renderIn(this)
                }
            }
        }
    }
}
