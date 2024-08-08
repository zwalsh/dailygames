package sh.zachwal.dailygames.shared_html

import kotlinx.html.DIV
import kotlinx.html.div
import kotlinx.html.h1

fun DIV.card(
    cardHeader: String? = null,
    cardTitle: String? = null,
    classes: String = "mx-3 mt-4 h-100",
    cardBody: DIV.() -> Unit,
) {
    div(classes = "card $classes") {
        cardHeader?.let {
            div(classes = "card-header") {
                +cardHeader
            }
        }
        div(classes = "card-body") {
            cardTitle?.let {
                h1(classes = "card-title") { +cardTitle }
            }
            cardBody()
        }
    }
}
