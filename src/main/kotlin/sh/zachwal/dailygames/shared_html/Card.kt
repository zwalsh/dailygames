package sh.zachwal.dailygames.shared_html

import kotlinx.html.DIV
import kotlinx.html.div
import kotlinx.html.h1

fun DIV.card(
    cardHeader: String? = null,
    cardHeaderClasses: String = "",
    cardTitle: String? = null,
    cardTitleClasses: String = "",
    classes: String = "mx-3 mt-4",
    cardBody: DIV.() -> Unit,
) {
    div(classes = "card $classes") {
        cardHeader?.let {
            h1(classes = "card-header $cardHeaderClasses") {
                +cardHeader
            }
        }
        div(classes = "card-body") {
            cardTitle?.let {
                h1(classes = "card-title $cardTitleClasses") { +cardTitle }
            }
            cardBody()
        }
    }
}
