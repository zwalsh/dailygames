package sh.zachwal.dailygames.leaderboard.views

import kotlinx.html.HTML
import kotlinx.html.body
import kotlinx.html.head
import kotlinx.html.title
import sh.zachwal.dailygames.db.jdbi.puzzle.Game
import sh.zachwal.dailygames.nav.NavItem
import sh.zachwal.dailygames.nav.NavView
import sh.zachwal.dailygames.shared_html.HTMLView
import sh.zachwal.dailygames.shared_html.darkMode
import sh.zachwal.dailygames.shared_html.headSetup

data class GameLeaderboardView(
    val username: String,
    val game: Game
) : HTMLView<HTML>() {

    val nav = NavView(username = username, currentActiveNavItem = NavItem.LEADERBOARD)

    override fun HTML.render() {
        head {
            title("Daily Games - Leaderboard - ${game.displayName()}")
            headSetup()
        }
        body {
            darkMode()
            nav.renderIn(this)
        }
    }
}
