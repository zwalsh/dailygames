package sh.zachwal.dailygames.home.views

import kotlinx.html.DIV
import kotlinx.html.div
import kotlinx.html.h3
import kotlinx.html.table
import kotlinx.html.tbody
import kotlinx.html.td
import kotlinx.html.th
import kotlinx.html.tr
import sh.zachwal.dailygames.shared_html.HTMLView
import java.util.SortedMap

data class DailyLeaderboardView(
    val dailyPerformances: SortedMap<String, Int>
) : HTMLView<DIV>() {
    override fun DIV.render() {
        div(classes = "card mx-3 my-1") {
            div(classes = "card-body bg-secondary-subtle") {
                h3(classes = "card-title text-center") {
                    +"Daily Leaderboard"
                }
                table(classes = "table") {
                    tbody {
                        dailyPerformances.entries.forEach { (user, score) ->
                            tr {
                                th { +user }
                                td { +score.toString() }
                            }
                        }
                    }
                }
            }
        }
    }
}