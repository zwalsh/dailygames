package sh.zachwal.dailygames.chat.views

import kotlinx.html.HTML
import kotlinx.html.a
import kotlinx.html.body
import kotlinx.html.div
import kotlinx.html.head
import kotlinx.html.id
import kotlinx.html.p
import kotlinx.html.script
import kotlinx.html.style
import kotlinx.html.title
import sh.zachwal.dailygames.db.jdbi.puzzle.Game
import sh.zachwal.dailygames.nav.NavView
import sh.zachwal.dailygames.shared_html.HTMLView
import sh.zachwal.dailygames.shared_html.card
import sh.zachwal.dailygames.shared_html.darkMode
import sh.zachwal.dailygames.shared_html.headSetup
import sh.zachwal.dailygames.shared_html.jquery
import java.time.Instant

data class ChatView constructor(
    val username: String,
    val game: Game,
    val puzzleNumber: Int,
    val updateTimeString: String,
    val chatFeedItems: List<ChatFeedItemView>,
    val isCommentDisabled: Boolean,
    val navView: NavView,
) : HTMLView<HTML>() {

    override fun HTML.render() {
        head {
            title {
                +"Daily Games - ${game.displayName()} #$puzzleNumber"
            }
            headSetup()
            jquery()
        }
        body {
            darkMode()
            navView.renderIn(this)
            div(classes = "container") {
                div(classes = "row") {
                    id = "chat-feed"
                    // Render the "hidden" chat item for the frontend to copy
                    ChatItemView(
                        username = "",
                        text = "",
                        timestampText = "",
                        instantSubmitted = Instant.now(),
                        hidden = true
                    ).renderIn(this)

                    chatFeedItems.forEach {
                        it.renderIn(this)
                    }
                }
                div(classes = "row mb-2") {
                    div(classes = "col") {
                        card(classes = "mx-3") {
                            ChatSubmitFormView(
                                game = game,
                                puzzleNumber = puzzleNumber,
                                isCommentDisabled = isCommentDisabled
                            ).renderIn(this)
                        }
                    }
                }
                div(classes = "row mb-1") {
                    div(classes = "col text-center") {
                        p(classes = "text-secondary mb-0 text-center fst-italic") {
                            style = "font-size: 0.8rem;"
                            +updateTimeString
                        }
                    }
                }
                div(classes = "row mb-2") {
                    a(href = "#", classes = "text-center text-white") {
                        +"Back to top"
                    }
                }
                script {
                    src = "/static/src/js/chat.js"
                }
            }
        }
    }
}
