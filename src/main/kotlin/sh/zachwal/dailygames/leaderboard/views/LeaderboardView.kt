package sh.zachwal.dailygames.leaderboard.views

import kotlinx.html.HTML
import kotlinx.html.body
import kotlinx.html.head
import kotlinx.html.title
import sh.zachwal.dailygames.nav.NavItem
import sh.zachwal.dailygames.nav.NavView
import sh.zachwal.dailygames.shared_html.HTMLView
import sh.zachwal.dailygames.shared_html.darkMode
import sh.zachwal.dailygames.shared_html.headSetup

data class LeaderboardView(
    val username: String,
) : HTMLView<HTML>() {

    val nav = NavView(username = username, currentActiveNavItem = NavItem.LEADERBOARD)

    override fun HTML.render() {
        head {
            title("Daily Games - Leaderboard")
            headSetup()
        }
        body {
            darkMode()
            nav.renderIn(this)
        }
    }
}
