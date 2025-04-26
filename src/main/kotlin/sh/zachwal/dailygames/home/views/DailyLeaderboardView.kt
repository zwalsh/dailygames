package sh.zachwal.dailygames.home.views

import kotlinx.html.DIV
import kotlinx.html.canvas
import kotlinx.html.div
import kotlinx.html.h1
import kotlinx.html.id
import sh.zachwal.dailygames.shared_html.HTMLView

object DailyLeaderboardView : HTMLView<DIV>() {
    override fun DIV.render() {
        div(classes = "card mx-3 h-100") {
            div(classes = "card-body bg-secondary-subtle") {
                h1(classes = "card-title text-center") {
                    +"Daily Leaderboard"
                }
                canvas {
                    id = "daily-leaderboard"
                }
            }
        }
    }
}
