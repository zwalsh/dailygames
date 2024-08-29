package sh.zachwal.dailygames.home.views

import kotlinx.html.DIV
import kotlinx.html.a
import kotlinx.html.div
import kotlinx.html.h1
import kotlinx.html.p
import kotlinx.html.span
import kotlinx.html.style
import sh.zachwal.dailygames.shared_html.HTMLView

data class ResultFeedItemView(
    val username: String,
    val resultTitle: String,
    val chatHref: String,
    val shareText: String,
    val timestampText: String,
) : HTMLView<DIV>() {
    override fun DIV.render() {
        div(classes = "col-12 col-sm-6 col-md-4 mt-2 mb-4") {
            div(classes = "card mx-3") {
                div(classes = "card-header") {
                    h1(classes = "fs-5 my-1") {
                        +"$username's "
                        a(href = chatHref) {
                            +resultTitle
                        }
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
