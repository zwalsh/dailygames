package sh.zachwal.dailygames.leaderboard.views

import kotlinx.html.HTML
import kotlinx.html.body
import kotlinx.html.div
import kotlinx.html.h1
import kotlinx.html.head
import kotlinx.html.p
import kotlinx.html.script
import kotlinx.html.title
import sh.zachwal.dailygames.nav.NavView
import sh.zachwal.dailygames.shared_html.HTMLView
import sh.zachwal.dailygames.shared_html.darkMode
import sh.zachwal.dailygames.shared_html.headSetup
import sh.zachwal.dailygames.shared_html.jquery

data class LeaderboardView(
    val nav: NavView,
) : HTMLView<HTML>() {

    override fun HTML.render() {
        head {
            title("Daily Games - Leaderboard")
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
                            +"Leaderboard"
                        }
                    }
                }
                BasicScoreHintView(
                    scoringText = """
                        Overall performance across all games. 
                        See individual leaderboards for scoring information.
                    """.trimIndent()
                ).renderIn(this)
                SectionHeaderView("All Time").renderIn(this)
                div(classes = "row") {
                    ChartView("all-time-points", "Total Points").renderIn(this)
                    ChartView("all-time-games", "Games Played").renderIn(this)
                    ChartView("all-time-average", "Average Points").renderIn(this)
                }
                SectionHeaderView("Past Thirty Days").renderIn(this)
                div(classes = "row") {
                    ChartView("thirty-days-points", "Total Points").renderIn(this)
                    ChartView("thirty-days-games", "Games Played").renderIn(this)
                    ChartView("thirty-days-average", "Average Points").renderIn(this)
                }
            }
            script {
                src = "/static/src/js/leaderboard.js"
            }
        }
    }
}
