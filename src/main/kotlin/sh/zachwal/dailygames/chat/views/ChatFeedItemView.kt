package sh.zachwal.dailygames.chat.views

import kotlinx.html.DIV
import kotlinx.html.div
import kotlinx.html.h1
import kotlinx.html.i
import kotlinx.html.id
import kotlinx.html.p
import kotlinx.html.span
import kotlinx.html.style
import sh.zachwal.dailygames.shared_html.HTMLView
import java.time.Instant

sealed class ChatFeedItemView : HTMLView<DIV>() {
    abstract val instantSubmitted: Instant
}

data class ResultItemView(
    val username: String,
    val shareText: String,
    val timestampText: String,
    override val instantSubmitted: Instant,
) : ChatFeedItemView() {
    override fun DIV.render() {
        div(classes = "col-12 col-sm-6 col-md-4 mt-2 mb-4") {
            div(classes = "card mx-3") {
                div(classes = "card-header bg-secondary-subtle") {
                    h1(classes = "fs-5 my-1") {
                        +username
                    }
                    p(classes = "text-secondary mb-0") {
                        style = "font-size: 0.9rem;"
                        +timestampText
                    }
                }
                div(classes = "card-body bg-dark-subtle") {
                    span(classes = "user-select-all") {
                        style = "white-space: pre-wrap;"
                        +shareText
                    }
                }
                // TODO add footer with score & reactions?
            }
        }
    }
}

data class ChatItemView(
    val username: String,
    val text: String,
    val timestampText: String,
    override val instantSubmitted: Instant,
    val hidden: Boolean = false,
) : ChatFeedItemView() {
    override fun DIV.render() {
        div(classes = "col-12 col-sm-6 col-md-4 mt-2 mb-4 ${if (hidden) "d-none" else ""}") {
            if (hidden) {
                id = "chat-to-copy"
            }
            div(classes = "card mx-3") {
                div(classes = "card-header") {
                    h1(classes = "fs-5 my-1 chat-username") {
                        +username
                    }
                    p(classes = "text-secondary mb-0 chat-displaytime") {
                        style = "font-size: 0.9rem;"
                        +timestampText
                    }
                }
                div(classes = "card-body") {
                    span(classes = "chat-text") {
                        style = "white-space: pre-wrap;"
                        +text
                    }
                }
                // TODO add footer with reactions?
            }
        }
    }
}

data class HiddenChatItemView(
    val username: String,
    val timestampText: String,
    override val instantSubmitted: Instant,
) : ChatFeedItemView() {
    override fun DIV.render() {
        div(classes = "col-12 col-sm-6 col-md-4 mt-2 mb-4") {
            div(classes = "card mx-3") {
                div(classes = "card-header") {
                    h1(classes = "fs-5 my-1") {
                        +username
                    }
                    p(classes = "text-secondary mb-0") {
                        style = "font-size: 0.9rem;"
                        +timestampText
                    }
                }
                div(classes = "card-body text-center my-3") {
                    attributes["data-bs-toggle"] = "tooltip"
                    attributes["data-bs-title"] = "Submit a solution to see comments!"
                    i(classes = "bi bi-eye-slash-fill text-body-tertiary fs-1")
                }
                // TODO add footer with reactions?
            }
        }
    }
}
