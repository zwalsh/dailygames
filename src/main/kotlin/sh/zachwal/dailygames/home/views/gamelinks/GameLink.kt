package sh.zachwal.dailygames.home.views.gamelinks

import kotlinx.html.DIV
import kotlinx.html.HtmlBlockInlineTag
import kotlinx.html.a
import kotlinx.html.div
import kotlinx.html.i
import kotlinx.html.role
import kotlinx.html.span
import kotlinx.html.style
import sh.zachwal.dailygames.db.jdbi.puzzle.Game
import sh.zachwal.dailygames.shared_html.HTMLView

data class GameLinkView(
    val game: Game,
    val isNew: Boolean,
) : HTMLView<DIV>() {
    override fun DIV.render() {
        div(classes = "col") {
            a(href = game.href(), classes = "btn btn-secondary position-relative", target = "_blank") {
                role = "button"
                style = "white-space: nowrap;"
                +"${game.emoji()} ${game.displayName()}"
                i(classes = "bi bi-box-arrow-up-right ms-2")
                if (isNew) {
                    NewBadgeView.renderIn(this)
                }
                attributes["data-umami-event"] = "Game Link"
                attributes["data-umami-event-game"] = game.displayName()
            }
        }
    }
}

private object NewBadgeView : HTMLView<HtmlBlockInlineTag>() {
    override fun HtmlBlockInlineTag.render() {
        span(classes = "position-absolute top-0 start-100 translate-middle") {
            i(classes = "bi bi-stars text-warning fs-4")
        }
    }
}
