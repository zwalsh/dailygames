package sh.zachwal.dailygames.home.views

import kotlinx.html.BODY
import kotlinx.html.button
import kotlinx.html.div
import kotlinx.html.h2
import kotlinx.html.i
import kotlinx.html.id
import kotlinx.html.p
import sh.zachwal.dailygames.shared_html.HTMLView

private const val SHARE_URL = "https://daily.zachwal.sh"

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
                        div(classes = "border border-dark-subtle rounded p-4 w-100") {
                            shareTextLines.forEach { line ->
                                p(classes = "fs-4 mb-0") {
                                    +line
                                }
                            }
                        }
                    }
                    div(classes = "modal-footer border-top-0 justify-content-center pt-0") {
                        button(classes = "btn btn-secondary") {
                            id = "copy-share-text-button"
                            attributes["data-bs-toggle"] = "tooltip"
                            attributes["data-bs-title"] = "Copied!"
                            +"Copy"
                            i(classes = "bi bi-copy ms-2") {}
                        }
                    }
                    div(classes = "d-none") {
                        id = "share-text"
                        +(shareTextLines + SHARE_URL).joinToString("\n")
                    }
                }
            }
        }
    }
}
