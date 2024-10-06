package sh.zachwal.dailygames.leaderboard.views

import kotlinx.html.HTML
import kotlinx.html.body
import kotlinx.html.div
import kotlinx.html.h1
import kotlinx.html.head
import kotlinx.html.p
import kotlinx.html.title
import sh.zachwal.dailygames.nav.NavItem
import sh.zachwal.dailygames.nav.NavView
import sh.zachwal.dailygames.shared_html.HTMLView
import sh.zachwal.dailygames.shared_html.darkMode
import sh.zachwal.dailygames.shared_html.headSetup

data class LeaderboardView(
    val nav: NavView,
) : HTMLView<HTML>() {

    override fun HTML.render() {
        head {
            title("Daily Games - Leaderboard")
            headSetup()
        }
        body {
            darkMode()
            nav.renderIn(this)
            div(classes = "container") {
                h1 {
                    +"Sorry"
                }
                p {
                    +"I haven't built this yet"
                }
                p {
                    +"(But the other leaderboards should work)"
                }
            }
        }
    }
}
