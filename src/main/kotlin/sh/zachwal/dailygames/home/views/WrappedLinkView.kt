package sh.zachwal.dailygames.home.views

import kotlinx.html.DIV
import kotlinx.html.a
import kotlinx.html.div
import kotlinx.html.i
import kotlinx.html.strong
import sh.zachwal.dailygames.shared_html.HTMLView

data class WrappedLinkView(
    val year: Int,
) : HTMLView<DIV>() {
    override fun DIV.render() {
        div(classes = "row mb-3 px-3") {
            div(classes = "col d-flex justify-content-center") {
                a(classes = "btn btn-primary", href = "/wrapped/$year") {
                    i(classes = "bi bi-stars text-warning me-2")
                    strong {
                        +"Daily Games Wrapped, $year"
                    }
                    i(classes = "bi bi-stars text-warning ms-2")
                }
            }
        }
    }
}
