package sh.zachwal.dailygames.leaderboard.views

import kotlinx.html.DIV
import kotlinx.html.a
import kotlinx.html.button
import kotlinx.html.div
import kotlinx.html.i
import kotlinx.html.span
import sh.zachwal.dailygames.shared_html.HTMLView

sealed class ScoreHintView : HTMLView<DIV>()

data class BasicScoreHintView(
    val scoringText: String
) : ScoreHintView() {
    override fun DIV.render() {
        div(classes = "row") {
            div(classes = "col") {
                div(classes = "alert alert-primary alert-dismissible fade show mx-4") {
                    i(classes = "bi bi-info-circle")
                    span(classes = "mx-2") {
                        +scoringText
                    }
                    button(classes = "btn-close") {
                        attributes["data-bs-dismiss"] = "alert"
                        attributes["aria-label"] = "Close"
                    }
                }
            }
        }
    }
}

class TravleScoreHintView : ScoreHintView() {
    override fun DIV.render() {
        div(classes = "row") {
            div(classes = "col") {
                div(classes = "alert alert-primary alert-dismissible fade show mx-4") {
                    i(classes = "bi bi-info-circle")
                    span(classes = "mx-2") {
                        +"Scoring: Excess guesses left after reaching the destination. 1 point for finishing. See "
                        a(href = "https://travle.earth/extra_info") {
                            +"Travle FAQ"
                        }
                        +"."
                    }
                    button(classes = "btn-close") {
                        attributes["data-bs-dismiss"] = "alert"
                        attributes["aria-label"] = "Close"
                    }
                }
            }
        }
    }
}
