package sh.zachwal.dailygames.home.views

import kotlinx.html.DIV
import kotlinx.html.div
import kotlinx.html.h1
import kotlinx.html.p
import kotlinx.html.span
import kotlinx.html.style
import sh.zachwal.dailygames.shared_html.HTMLView

data class ResultFeedItemView(
    val username: String,
    val resultTitle: String,
    val shareText: String,
    val timestampText: String,
) : HTMLView<DIV>() {
    override fun DIV.render() {
        div(classes = "row mt-2 mb-4") {
            div(classes = "col") {
                div(classes = "card mx-3") {
                    div(classes = "card-header") {
                        h1(classes = "fs-5 my-1") {
                            +"$username's $resultTitle"
                        }
                        p(classes = "text-secondary mb-0") {
                            style = "font-size: 0.9rem;"
                            +timestampText
                        }
                    }
                    div(classes = "card-body") {
                        span {
                            style = "white-space: pre-wrap;"
                            +shareText
                        }
                    }
                }
            }
        }
    }
}
