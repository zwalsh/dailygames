package sh.zachwal.dailygames.leaderboard.views

import kotlinx.html.DIV
import kotlinx.html.HTML
import kotlinx.html.body
import kotlinx.html.canvas
import kotlinx.html.div
import kotlinx.html.h1
import kotlinx.html.h2
import kotlinx.html.head
import kotlinx.html.id
import kotlinx.html.script
import kotlinx.html.title
import sh.zachwal.dailygames.db.jdbi.puzzle.Game
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
                SectionHeaderView("All Time").renderIn(this)
                div(classes = "row") {
                    ChartView("game-leaderboard-all-time-points", "Total Points").renderIn(this)
                    ChartView("game-leaderboard-all-time-games", "Games Played").renderIn(this)
                    ChartView("game-leaderboard-all-time-average", "Average Points").renderIn(this)
                }
                SectionHeaderView("Past 30 Days").renderIn(this)
                div(classes = "row") {
                    ChartView("game-leaderboard-thirty-days-points", "Total Points").renderIn(this)
                    ChartView("game-leaderboard-thirty-days-games", "Games Played").renderIn(this)
                    ChartView("game-leaderboard-thirty-days-average", "Average Points").renderIn(this)
                }
            }
            script {
                src = "/static/src/js/leaderboard.js"
            }
        }
    }
}

data class SectionHeaderView(
    val text: String,
) : HTMLView<DIV>() {

    override fun DIV.render() {
        div(classes = "row") {
            div(classes = "col") {
                h2(classes = "text-center fs-3 mb-2") {
                    +text
                }
            }
        }
    }
}

data class ChartView(
    val canvasId: String,
    val header: String,
) : HTMLView<DIV>() {

    override fun DIV.render() {
        div(classes = "col-12 col-md-6 mb-4") {
            card(header, cardHeaderClasses = "text-center fs-4", classes = "mx-3") {
                canvas {
                    id = canvasId
                }
            }
        }
    }
}
