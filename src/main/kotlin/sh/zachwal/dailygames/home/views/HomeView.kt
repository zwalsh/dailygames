package sh.zachwal.dailygames.home.views

import kotlinx.html.HTML
import kotlinx.html.body
import kotlinx.html.div
import kotlinx.html.h1
import kotlinx.html.head
import kotlinx.html.title
import sh.zachwal.dailygames.nav.NavView
import sh.zachwal.dailygames.shared_html.HTMLView
import sh.zachwal.dailygames.shared_html.darkMode
import sh.zachwal.dailygames.shared_html.headSetup

data class HomeView(
    private val name: String
) : HTMLView<HTML>() {
    override fun HTML.render() {
        head {
            title("Daily Games")
            headSetup()
        }
        body {
            darkMode()
            NavView.renderIn(this)
            div(classes = "container") {
                h1 {
                    +"Hello, world! (test)"
                }
            }
        }
    }
}
