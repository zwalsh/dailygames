package sh.zachwal.dailygames.home.views

import kotlinx.html.BODY
import kotlinx.html.button
import kotlinx.html.div
import kotlinx.html.id
import sh.zachwal.dailygames.shared_html.HTMLView

data class ShareTextModalView(
    val text: String,
) : HTMLView<BODY>() {
    override fun BODY.render() {
        div(classes = "modal") {
            id = "share-text-modal"
            div(classes = "modal-dialog modal-dialog-centered modal-md") {
                div(classes = "modal-content") {
                    div(classes = "modal-header") {
                        div(classes = "modal-title") {
                            +"Share Text"
                        }
                        button(classes = "btn-close") {
                            attributes["data-bs-dismiss"] = "modal"
                        }
                    }
                    div(classes = "modal-body") {
                        +text
                    }
                }
            }
        }
    }
}
