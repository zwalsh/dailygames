package sh.zachwal.dailygames.chat.views

import kotlinx.html.DIV
import kotlinx.html.div
import kotlinx.html.h6
import sh.zachwal.dailygames.shared_html.HTMLView

data class AnswerView(
    val answerText: String,
) : HTMLView<DIV>() {
    override fun DIV.render() {
        div(classes = "row justify-content-center") {
            div(classes = "col d-flex justify-content-center") {
                h6(classes = "bg-secondary border border-bg-secondary rounded text-center d-inline-block p-2 mb-0") {
                    +answerText
                }
            }
        }
    }
}
