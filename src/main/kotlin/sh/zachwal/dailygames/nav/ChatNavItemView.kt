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
    val chatCounts: Map<Game, Int>,
) : HTMLView<UL>() {

    private val textStyling = if (isActive) {
        "text-white"
    } else {
        "text-secondary"
    }

    private val chatTotal = chatCounts.values.sum()

    override fun UL.render() {
        li {
            div(classes = "nav-link $textStyling text-small") {
                attributes["data-bs-toggle"] = "dropdown"
                i(classes = "bi bi-chat-left-dots-fill d-block text-center fs-3 position-relative") {
                    if (chatTotal > 0) {
                        ChatNavBadgeView(chatTotal, isAbsolute = true).renderIn(this)
                    }
                }
                +"Chat"
            }
            ul(classes = "dropdown-menu") {
                Game.values().forEach { game ->
                    val gameChatTotal = chatCounts[game] ?: 0

                    li {
                        a(
                            href = "/game/${game.name.lowercase()}/puzzle",
                            classes = "dropdown-item d-flex justify-content-between align-items-center"
                        ) {
                            +"${game.emoji()} ${game.displayName()}"
                            if (gameChatTotal > 0) {
                                ChatNavBadgeView(gameChatTotal, isAbsolute = false).renderIn(this)
                            }
                        }
                    }
                }
            }
        }
    }
}
