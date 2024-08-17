package sh.zachwal.dailygames.leaderboard.views

import kotlinx.html.HTML
import kotlinx.html.body
import kotlinx.html.canvas
import kotlinx.html.div
import kotlinx.html.h1
import kotlinx.html.head
import kotlinx.html.id
import kotlinx.html.title
import sh.zachwal.dailygames.db.jdbi.puzzle.Game
import sh.zachwal.dailygames.nav.NavItem
import sh.zachwal.dailygames.nav.NavView
import sh.zachwal.dailygames.shared_html.HTMLView
import sh.zachwal.dailygames.shared_html.card
import sh.zachwal.dailygames.shared_html.darkMode
import sh.zachwal.dailygames.shared_html.headSetup

data class GameLeaderboardView(
    val username: String,
    val game: Game
) : HTMLView<HTML>() {

    val nav = NavView(username = username, currentActiveNavItem = NavItem.LEADERBOARD)

    override fun HTML.render() {
        head {
            title("Daily Games - ${game.displayName()} Leaderboard")
            headSetup()
        }
        body {
            darkMode()
            nav.renderIn(this)
            div(classes = "container") {
                div(classes = "row") {
                    div(classes = "col") {
                        h1(classes = "text-center fs-2") {
                            +"${game.emoji()} ${game.displayName()} Leaderboard ${game.emoji()}"
                        }
                    }
                }
                div(classes = "row") {
                    div(classes = "col") {
                        card("All Time", cardHeaderClasses = "text-center fs-3") {
                            canvas {
                                id = "game-leaderboard-all-time"
                            }
                        }
                    }
                }
                div(classes = "row") {
                    div(classes = "col") {
                        card("Past 30 Days") {
                            canvas {
                                id = "game-leaderboard-past-30-days"
                            }
                        }
                    }
                }
            }
        }
    }
}
