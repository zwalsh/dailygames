package sh.zachwal.dailygames.nav

import kotlinx.html.UL
import kotlinx.html.a
import kotlinx.html.div
import kotlinx.html.i
import kotlinx.html.li
import kotlinx.html.ul
import sh.zachwal.dailygames.db.jdbi.puzzle.Game
import sh.zachwal.dailygames.shared_html.HTMLView

data class ChatNavItemView(
    val isActive: Boolean,
    val chatNavBadgeView: ChatNavBadgeView?
) : HTMLView<UL>() {

    private val textStyling = if (isActive) {
        "text-white"
    } else {
        "text-secondary"
    }

    override fun UL.render() {
        li {
            div(classes = "nav-link $textStyling text-small") {
                attributes["data-bs-toggle"] = "dropdown"
                i(classes = "bi bi-chat-left-dots-fill d-block text-center fs-3 position-relative") {
                    chatNavBadgeView?.renderIn(this)
                }
                +"Chat"
            }
            ul(classes = "dropdown-menu") {
                Game.values().forEach { game ->
                    li {
                        a(href = "/game/${game.name.lowercase()}/puzzle", classes = "dropdown-item") {
                            +"${game.emoji()} ${game.displayName()}"
                        }
                    }
                }
            }
        }
    }
}
