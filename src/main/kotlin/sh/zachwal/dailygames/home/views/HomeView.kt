package sh.zachwal.dailygames.home.views

import kotlinx.html.HTML
import kotlinx.html.body
import kotlinx.html.div
import kotlinx.html.h1
import kotlinx.html.head
import kotlinx.html.title
import sh.zachwal.dailygames.nav.NavItem
import sh.zachwal.dailygames.nav.NavView
import sh.zachwal.dailygames.shared_html.HTMLView
import sh.zachwal.dailygames.shared_html.darkMode
import sh.zachwal.dailygames.shared_html.headSetup

data class HomeView(
    val username: String
) : HTMLView<HTML>() {

    private val nav = NavView(username = username, currentActiveNavItem = NavItem.HOME)

    override fun HTML.render() {
        head {
            title("Daily Games")
            headSetup()
        }
        body {
            darkMode()
            nav.renderIn(this)
            div(classes = "container") {
                h1 {
                    +"Hello, world! (test)"
                }
            }
        }
    }
}
