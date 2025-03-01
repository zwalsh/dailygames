package sh.zachwal.dailygames.chat.views

import kotlinx.html.ButtonType
import kotlinx.html.DIV
import kotlinx.html.FormMethod.post
import kotlinx.html.a
import kotlinx.html.button
import kotlinx.html.div
import kotlinx.html.form
import kotlinx.html.id
import kotlinx.html.textArea
import sh.zachwal.dailygames.chat.CHAT_TEXT_ID
import sh.zachwal.dailygames.chat.chatLink
import sh.zachwal.dailygames.db.jdbi.puzzle.Game
import sh.zachwal.dailygames.shared_html.HTMLView
import sh.zachwal.dailygames.shared_html.view.SpinnerView

data class ChatSubmitFormView(
    val game: Game,
    val puzzleNumber: Int,
    val isCommentDisabled: Boolean
) : HTMLView<DIV>() {
    override fun DIV.render() {
        form(method = post, action = "${chatLink(game, puzzleNumber)}/comment") {
            id = "chat-form"
            div(classes = "mb-3") {
                textArea(classes = "form-control", rows = "3") {
                    if (isCommentDisabled) {
                        attributes["disabled"] = "true"
                        attributes["placeholder"] = "Submit a solution to comment!"
                        attributes["data-bs-toggle"] = "tooltip"
                        attributes["data-bs-title"] = "Submit a solution to comment!"
                    }

                    id = CHAT_TEXT_ID
                    name = CHAT_TEXT_ID
                }
            }
            // TODO live updating character count
            div(classes = "d-flex justify-content-end") {
                if (isCommentDisabled) {
                    a(href = "/", classes = "mx-2") {
                        +"Submit a solution"
                    }
                }
                div {
                    if (isCommentDisabled) {
                        attributes["data-bs-toggle"] = "tooltip"
                        attributes["data-bs-title"] = "Submit a solution to comment!"
                    }
                    button(classes = "btn btn-primary ${if (isCommentDisabled) "disabled" else ""}") {
                        id = "submit-button"
                        attributes["data-umami-event"] = "Submit Comment"
                        type = ButtonType.submit
                        SpinnerView.renderIn(this)
                        +"Comment"
                    }
                }
            }
        }
    }
}
