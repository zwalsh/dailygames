package sh.zachwal.dailygames.chat

import io.ktor.application.call
import io.ktor.html.respondHtml
import io.ktor.http.HttpStatusCode
import io.ktor.response.respond
import io.ktor.routing.Routing
import io.ktor.routing.get
import org.slf4j.LoggerFactory
import sh.zachwal.dailygames.auth.currentUser
import sh.zachwal.dailygames.controller.Controller
import sh.zachwal.dailygames.results.ResultService
import sh.zachwal.dailygames.roles.approvedUserRoute
import sh.zachwal.dailygames.users.UserService
import sh.zachwal.dailygames.utils.extractGameFromPathParams
import javax.inject.Inject
import javax.inject.Singleton

@Controller
@Singleton
class ChatController @Inject constructor(
    private val resultService: ResultService,
    private val userService: UserService,
    private val chatService: ChatService,
) {

    private val logger = LoggerFactory.getLogger(ChatController::class.java)

    fun Routing.puzzlePage() {
        approvedUserRoute("/game/{game}/puzzle/{puzzleNumber}") {
            get {
                val currentUser = currentUser(call, userService)
                val game = call.extractGameFromPathParams() ?: run {
                    call.respond(HttpStatusCode.NotFound)
                    return@get
                }
                val puzzleNumber = call.parameters["puzzleNumber"]?.toIntOrNull() ?: run {
                    call.respond(HttpStatusCode.NotFound)
                    return@get
                }

                logger.info("User ${currentUser.username} loaded puzzle $puzzleNumber in game $game")

                val chatView = chatService.chatView(
                    currentUser.username,
                    game,
                    puzzleNumber
                )

                call.respondHtml {
                    chatView.renderIn(this)
                }
            }
        }
    }

    fun Routing.latestPuzzlePage() {
        approvedUserRoute("/game/{game}/puzzle") {
            get {
                val currentUser = currentUser(call, userService)
                val game = call.extractGameFromPathParams() ?: run {
                    call.respond(HttpStatusCode.NotFound)
                    return@get
                }

                logger.info("User ${currentUser.username} loaded latest puzzle in game $game")

                val chatView = chatService.chatViewLatest(username = currentUser.username, game = game)

                call.respondHtml {
                    chatView.renderIn(this)
                }
            }
        }
    }
}
