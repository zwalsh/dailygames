package sh.zachwal.dailygames.nav

import kotlinx.html.HtmlBlockInlineTag
import kotlinx.html.span
import kotlinx.html.style
import sh.zachwal.dailygames.shared_html.HTMLView

private const val ABSOLUTE_POSITION_CLASSES = "position-absolute top-0 start-100 translate-middle"

data class ChatNavBadgeView(
    val count: Int,
    val isAbsolute: Boolean,
) : HTMLView<HtmlBlockInlineTag>() {
    override fun HtmlBlockInlineTag.render() {
        val classes = "badge rounded-pill bg-danger fw-bold fst-normal " + if (isAbsolute) ABSOLUTE_POSITION_CLASSES else ""
        span(classes = classes) {
            style = "font-size: 0.6rem;"
            +count.toString()
        }
    }
}
