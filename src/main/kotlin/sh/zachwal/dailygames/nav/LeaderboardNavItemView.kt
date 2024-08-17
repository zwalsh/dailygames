package sh.zachwal.dailygames.nav

import kotlinx.html.UL
import kotlinx.html.a
import kotlinx.html.i
import kotlinx.html.li
import kotlinx.html.ul
import sh.zachwal.dailygames.db.jdbi.puzzle.Game
import sh.zachwal.dailygames.shared_html.HTMLView

data class LeaderboardNavItemView(
    val isActive: Boolean
) : HTMLView<UL>() {

    private val textStyling = if (isActive) {
        "text-white"
    } else {
        "text-secondary"
    }

    override fun UL.render() {
        li {
            a(href = "/leaderboard", classes = "nav-link $textStyling text-small") {
                attributes["data-bs-toggle"] = "dropdown"
                i(classes = "bi bi-bar-chart-fill d-block text-center fs-3")
                +"Leaderboard"
            }
            ul(classes = "dropdown-menu") {
                li {
                    a(href = "/leaderboard", classes = "dropdown-item") {
                        +"Overall"
                    }
                }
                Game.values().forEach { game ->
                    li {
                        a(href = "/leaderboard/${game.name.lowercase()}", classes = "dropdown-item") {
                            +game.displayName()
                        }
                    }
                }
            }
        }
    }
}
