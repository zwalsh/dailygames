package sh.zachwal.dailygames.chat.views

import kotlinx.html.FormMethod.post
import kotlinx.html.HTML
import kotlinx.html.a
import kotlinx.html.body
import kotlinx.html.div
import kotlinx.html.form
import kotlinx.html.h1
import kotlinx.html.head
import kotlinx.html.i
import kotlinx.html.id
import kotlinx.html.p
import kotlinx.html.script
import kotlinx.html.style
import kotlinx.html.submitInput
import kotlinx.html.textArea
import kotlinx.html.title
import sh.zachwal.dailygames.chat.CHAT_TEXT_ID
import sh.zachwal.dailygames.chat.chatLink
import sh.zachwal.dailygames.db.jdbi.puzzle.Game
import sh.zachwal.dailygames.nav.NavItem
import sh.zachwal.dailygames.nav.NavView
import sh.zachwal.dailygames.shared_html.HTMLView
import sh.zachwal.dailygames.shared_html.card
import sh.zachwal.dailygames.shared_html.darkMode
import sh.zachwal.dailygames.shared_html.headSetup

data class ChatView constructor(
    val username: String,
    val game: Game,
    val puzzleNumber: Int,
    val updateTimeString: String,
    val chatFeedItems: List<ChatFeedItemView>,
    val prevLink: String? = null,
    val nextLink: String? = null,
    val isCommentDisabled: Boolean,
) : HTMLView<HTML>() {

    val navView = NavView(username = username, currentActiveNavItem = NavItem.CHAT)

    override fun HTML.render() {
        head {
            title {
                +"Daily Games - ${game.displayName()} #$puzzleNumber"
            }
            headSetup()
        }
        body {
            darkMode()
            navView.renderIn(this)
            div(classes = "container") {
                div(classes = "row mx-4") {
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
                        p(classes = "text-secondary mb-0 text-center fst-italic") {
                            style = "font-size: 0.8rem;"
                            +updateTimeString
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
                div(classes = "row") {
                    chatFeedItems.forEach {
                        it.renderIn(this)
                    }
                }
                div(classes = "row") {
                    div(classes = "col mb-4") {
                        card(cardTitle = "Comment", cardTitleClasses = "text-center fs-3", classes = "mx-3") {
                            form(method = post, action = "${chatLink(game, puzzleNumber)}/comment") {

                                div(classes = "mb-3") {
                                    textArea(classes = "form-control", rows = "5") {
                                        if (isCommentDisabled) {
                                            attributes["disabled"] = "true"
                                            attributes["placeholder"] = "Submit a solution to comment!"
                                        }

                                        id = CHAT_TEXT_ID
                                        name = CHAT_TEXT_ID
                                    }
                                }
                                // TODO live updating character count
                                div(classes = "d-flex justify-content-end") {
                                    div {
                                        if (isCommentDisabled) {
                                            attributes["data-bs-toggle"] = "tooltip"
                                            attributes["data-bs-title"] = "Submit a solution to comment!"
                                        }
                                        submitInput(classes = "btn btn-primary ${if (isCommentDisabled) "disabled" else ""}") {
                                            value = "Post"
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                script {
                    src = "/static/src/js/chat.js"
                }
            }
        }
    }
}
