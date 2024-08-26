package sh.zachwal.dailygames.chat.views

import kotlinx.html.HTML
import kotlinx.html.body
import kotlinx.html.div
import kotlinx.html.h1
import kotlinx.html.head
import kotlinx.html.title
import sh.zachwal.dailygames.db.jdbi.puzzle.Game
import sh.zachwal.dailygames.nav.NavItem
import sh.zachwal.dailygames.nav.NavView
import sh.zachwal.dailygames.shared_html.HTMLView
import sh.zachwal.dailygames.shared_html.darkMode
import sh.zachwal.dailygames.shared_html.headSetup

data class ChatView(
    val username: String,
    val game: Game,
    val puzzleNumber: Int,
    val chatFeedItems: List<ChatFeedItemView>
) : HTMLView<HTML>() {

    val navView = NavView(username = username, currentActiveNavItem = NavItem.HOME)

    override fun HTML.render() {
        head {
            title {
                +"Daily Games - ${game.displayName()} #$puzzleNumber"
            }
            headSetup()
        }
        body {
            darkMode()
            navView.renderIn(this)
            h1(classes = "text-center mt-4") {
                +"${game.displayName()} #$puzzleNumber"
            }
            div(classes = "container") {
                div(classes = "row") {
                    chatFeedItems.forEach {
                        it.renderIn(this)
                    }
                }
            }
        }
    }
}
