package sh.zachwal.dailygames.leaderboard.views

import kotlinx.html.DIV
import kotlinx.html.canvas
import kotlinx.html.div
import kotlinx.html.id
import sh.zachwal.dailygames.shared_html.HTMLView
import sh.zachwal.dailygames.shared_html.card

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
