package sh.zachwal.dailygames.home.views

import kotlinx.html.BODY
import kotlinx.html.button
import kotlinx.html.div
import kotlinx.html.h2
import kotlinx.html.id
import kotlinx.html.p
import kotlinx.html.span
import sh.zachwal.dailygames.shared_html.HTMLView

data class ShareTextModalView(
    val shareTextLines: List<String>,
) : HTMLView<BODY>() {
    override fun BODY.render() {
        div(classes = "modal") {
            id = "share-text-modal"
            div(classes = "modal-dialog modal-dialog-centered modal-md") {
                div(classes = "modal-content") {
                    div(classes = "modal-header border-bottom-0 pb-0") {
                        div(classes = "modal-title w-100 text-center") {
                            h2(classes = "fs-1 mb-0") {
                                +"Nice!"
                            }
                        }
                        button(classes = "btn-close") {
                            attributes["data-bs-dismiss"] = "modal"
                        }
                    }
                    div(classes = "modal-body d-flex justify-content-center") {
                        div(classes="border border-dark-subtle rounded p-4") {
                            shareTextLines.forEach { line ->
                                p (classes = "fs-3 mb-0") {
                                    +line
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
