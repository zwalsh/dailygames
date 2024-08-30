package sh.zachwal.dailygames.chat

import io.ktor.application.call
import io.ktor.html.respondHtml
import io.ktor.http.HttpStatusCode
import io.ktor.request.receiveParameters
import io.ktor.request.uri
import io.ktor.response.respond
import io.ktor.response.respondRedirect
import io.ktor.routing.Routing
import io.ktor.routing.get
import io.ktor.routing.post
import io.ktor.util.getOrFail
import org.slf4j.LoggerFactory
import sh.zachwal.dailygames.auth.currentUser
import sh.zachwal.dailygames.controller.Controller
import sh.zachwal.dailygames.results.ResultService
import sh.zachwal.dailygames.roles.approvedUserRoute
import sh.zachwal.dailygames.users.UserService
import sh.zachwal.dailygames.utils.extractGameFromPathParams
import javax.inject.Inject
import javax.inject.Singleton

const val CHAT_TEXT_ID = "chatTextId"

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

                val chatView = chatService.chatView(
                    currentUser = currentUser,
                    game = game,
                    puzzleNumber = puzzleNumber
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

                val chatView = chatService.chatViewLatest(currentUser = currentUser, game = game)

                call.respondHtml {
                    chatView.renderIn(this)
                }
            }
        }
    }

    fun Routing.commentSubmit() {
        approvedUserRoute("/game/{game}/puzzle/{puzzleNumber}/comment") {
            post {
                val currentUser = currentUser(call, userService)
                val game = call.extractGameFromPathParams() ?: run {
                    call.respond(HttpStatusCode.NotFound)
                    return@post
                }
                val puzzleNumber = call.parameters["puzzleNumber"]?.toIntOrNull() ?: run {
                    call.respond(HttpStatusCode.NotFound)
                    return@post
                }
                val parameters = call.receiveParameters()
                val chatText = parameters.getOrFail(CHAT_TEXT_ID)

                if (chatText.length > 500) {
                    call.respond(HttpStatusCode.BadRequest, "Comment is too long, must be 500 characters or less.")
                    return@post
                }

                chatService.insertChat(currentUser.id, game, puzzleNumber, chatText)
                logger.info("User ${currentUser.username} posted a comment on ${game.name} puzzle $puzzleNumber.")

                val chatPage = call.request.uri.removeSuffix("/comment")

                call.respondRedirect(chatPage)
            }
        }
    }
}
