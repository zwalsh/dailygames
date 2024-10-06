package sh.zachwal.dailygames.leaderboard.views

import kotlinx.html.HTML
import kotlinx.html.body
import kotlinx.html.canvas
import kotlinx.html.div
import kotlinx.html.h1
import kotlinx.html.head
import kotlinx.html.id
import kotlinx.html.script
import kotlinx.html.title
import sh.zachwal.dailygames.db.jdbi.puzzle.Game
import sh.zachwal.dailygames.nav.NavItem
import sh.zachwal.dailygames.nav.NavView
import sh.zachwal.dailygames.shared_html.HTMLView
import sh.zachwal.dailygames.shared_html.card
import sh.zachwal.dailygames.shared_html.darkMode
import sh.zachwal.dailygames.shared_html.headSetup
import sh.zachwal.dailygames.shared_html.jquery

data class GameLeaderboardView(
    val game: Game,
    val scoreHintView: ScoreHintView,
    val nav: NavView,
) : HTMLView<HTML>() {

    override fun HTML.render() {
        head {
            title("Daily Games - ${game.displayName()} Leaderboard")
            headSetup()
            script {
                src = "https://cdn.jsdelivr.net/npm/chart.js"
            }
            jquery()
        }
        body {
            darkMode()
            nav.renderIn(this)
            div(classes = "container mb-4") {
                div(classes = "row") {
                    div(classes = "col") {
                        h1(classes = "text-center fs-2 mb-4") {
                            +"${game.emoji()} ${game.displayName()} Leaderboard ${game.emoji()}"
                        }
                    }
                }
                scoreHintView.renderIn(this)
                div(classes = "row") {
                    div(classes = "col-12 col-md-6 mb-4") {
                        card("All Time", cardHeaderClasses = "text-center fs-3", classes = "mx-3") {
                            canvas {
                                id = "game-leaderboard-all-time"
                            }
                        }
                    }
                    div(classes = "col-12 col-md-6 mb-4") {
                        card("Past 30 Days", cardHeaderClasses = "text-center fs-3", classes = "mx-3") {
                            canvas {
                                id = "game-leaderboard-past-30-days"
                            }
                        }
                    }
                }
            }
            script {
                src = "/static/src/js/leaderboard.js"
            }
        }
    }
}
