package sh.zachwal.dailygames.home.views

import kotlinx.html.HTML
import kotlinx.html.body
import kotlinx.html.div
import kotlinx.html.h1
import kotlinx.html.head
import kotlinx.html.title
import sh.zachwal.dailygames.shared_html.HTMLView
import sh.zachwal.dailygames.shared_html.favicon
import sh.zachwal.dailygames.shared_html.mobileUI
import sh.zachwal.dailygames.shared_html.sentryScript

data class HomeView(
    private val name: String
): HTMLView<HTML>() {
    override fun HTML.render() {
        head {
            title("Daily Games")
            mobileUI()
            favicon()
            sentryScript()
        }
        body {
            div(classes = "container") {
                h1 {
                    +"Hello, world! (test)"
                }
            }
        }
    }
}
