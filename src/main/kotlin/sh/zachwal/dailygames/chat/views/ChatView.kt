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
import kotlinx.html.submitInput
import kotlinx.html.textArea
import kotlinx.html.title
import sh.zachwal.dailygames.chat.CHAT_TEXT_ID
import sh.zachwal.dailygames.db.jdbi.puzzle.Game
import sh.zachwal.dailygames.home.views.SHARE_TEXT_ID
import sh.zachwal.dailygames.nav.NavItem
import sh.zachwal.dailygames.nav.NavView
import sh.zachwal.dailygames.shared_html.HTMLView
import sh.zachwal.dailygames.shared_html.card
import sh.zachwal.dailygames.shared_html.darkMode
import sh.zachwal.dailygames.shared_html.headSetup

data class ChatView(
    val username: String,
    val game: Game,
    val puzzleNumber: Int,
    val chatFeedItems: List<ChatFeedItemView>,
    val prevLink: String? = null,
    val nextLink: String? = null,
) : HTMLView<HTML>() {

    val navView = NavView(username = username, currentActiveNavItem = NavItem.HOME)

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
                            form(method = post, action = "/game/${game.name.lowercase()}/puzzle/$puzzleNumber/comment") {

                                div(classes = "mb-3") {
                                    textArea(classes = "form-control", rows = "5") {
                                        id = CHAT_TEXT_ID
                                        name = CHAT_TEXT_ID
                                    }
                                }
                                div(classes = "d-flex justify-content-end") {
                                    submitInput(classes = "btn btn-primary") {
                                        value = "Post"
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
