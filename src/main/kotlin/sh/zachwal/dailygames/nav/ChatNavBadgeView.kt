package sh.zachwal.dailygames.nav

import kotlinx.html.I
import kotlinx.html.span
import kotlinx.html.style
import sh.zachwal.dailygames.shared_html.HTMLView

data class ChatNavBadgeView(
    val count: Int
) : HTMLView<I>() {
    override fun I.render() {
        span(classes = "position-absolute top-0 start-100 translate-middle badge rounded-pill bg-danger fw-bold") {
            style = "font-size: 0.6rem;"
            +count.toString()
        }
    }
}
