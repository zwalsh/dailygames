package sh.zachwal.dailygames.shared_html.view

import kotlinx.html.BUTTON
import kotlinx.html.i
import kotlinx.html.id
import sh.zachwal.dailygames.shared_html.HTMLView

object SpinnerView : HTMLView<BUTTON>() {
    override fun BUTTON.render() {
        i(classes = "bi bi-arrow-repeat me-2 d-none") {
            id = "submit-spinner"
        }
    }
}