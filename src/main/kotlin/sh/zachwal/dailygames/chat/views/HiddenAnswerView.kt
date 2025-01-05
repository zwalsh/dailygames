package sh.zachwal.dailygames.chat.views

import kotlinx.html.DIV
import kotlinx.html.div
import kotlinx.html.h6
import kotlinx.html.i
import sh.zachwal.dailygames.shared_html.HTMLView

object HiddenAnswerView : HTMLView<DIV>() {
    override fun DIV.render() {
        div(classes = "row justify-content-center") {
            div(classes = "col d-flex justify-content-center") {
                h6(classes = "bg-secondary border border-bg-secondary rounded text-center d-inline-block p-2 mb-0") {
                    attributes["data-bs-toggle"] = "tooltip"
                    attributes["data-bs-title"] = "Submit a solution to see the answer!"
                    i(classes = "bi bi-eye-slash-fill text-body-tertiary fs-6 me-2")
                    +"Answer"
                }
            }
        }
    }
}