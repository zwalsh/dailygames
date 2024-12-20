package sh.zachwal.dailygames.chat.views

import kotlinx.html.HEADER
import kotlinx.html.HTML
import kotlinx.html.a
import kotlinx.html.body
import kotlinx.html.div
import kotlinx.html.h1
import kotlinx.html.head
import kotlinx.html.i
import kotlinx.html.id
import kotlinx.html.p
import kotlinx.html.script
import kotlinx.html.style
import kotlinx.html.title
import sh.zachwal.dailygames.db.jdbi.puzzle.Game
import sh.zachwal.dailygames.nav.NavItem
import sh.zachwal.dailygames.nav.NavViewFactory
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
    val prevLink: String? = null,
    val nextLink: String? = null,
    val isCommentDisabled: Boolean,
    val navViewFactory: NavViewFactory,
) : HTMLView<HTML>() {

    val navView = navViewFactory.navView(
        username = username,
        currentActiveNavItem = NavItem.CHAT,
        insideNavItem = ChatNav(),
    )

    inner class ChatNav : HTMLView<HEADER>() {
        override fun HEADER.render() {
            div(classes = "row mx-4 py-2 border-top") {
                div(classes = "col-1 d-flex align-items-center") {
                    prevLink?.let { href ->
                        a(href = href, classes = "float-start text-white") {
                            i(classes = "bi bi-chevron-compact-left")
                        }
                    }
                }
                div(classes = "col-10") {
                    h1(classes = "text-center") {
                        +"${game.displayName()} #$puzzleNumber"
                    }
                }
                div(classes = "col-1 d-flex align-items-center") {
                    nextLink?.let { href ->
                        a(href = href, classes = "float-end text-white") {
                            i(classes = "bi bi-chevron-compact-right")
                        }
                    }
                }
            }
        }
    }

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
