package sh.zachwal.dailygames.home.views

import kotlinx.html.DIV
import kotlinx.html.div
import kotlinx.html.span
import kotlinx.html.style
import sh.zachwal.dailygames.shared_html.HTMLView
import sh.zachwal.dailygames.shared_html.card

data class ResultFeedItemView(
    val username: String,
    val resultTitle: String,
    val shareText: String,
) : HTMLView<DIV>() {
    override fun DIV.render() {
        div(classes = "row mt-2 mb-4") {
            div(classes = "col") {
                card(
                    cardHeader = "$username's $resultTitle",
                    cardHeaderClasses = "fs-5",
                    classes = "mx-3"
                ) {
                    span {
                        style = "white-space: pre-wrap;"
                        +shareText
                    }
                }
            }
        }
    }
}
