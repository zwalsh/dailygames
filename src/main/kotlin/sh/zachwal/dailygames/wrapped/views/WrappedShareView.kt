package sh.zachwal.dailygames.wrapped.views

import kotlinx.html.DIV
import kotlinx.html.button
import kotlinx.html.div
import kotlinx.html.i
import kotlinx.html.id
import kotlinx.html.strong
import sh.zachwal.dailygames.shared_html.HTMLView

data class WrappedShareView(
    val year: Int,
    val username: String,
) : HTMLView<DIV>() {
    override fun DIV.render() {
        div(classes = "row") {
            div(classes = "col mt-3 mx-3") {
                button(classes = "btn btn-primary w-100") {
                    id = "share-button"
                    attributes["data-year"] = year.toString()
                    attributes["data-username"] = username

                    i(classes = "bi bi-box-arrow-up me-2")
                    strong {
                        +"Share"
                    }
                }
            }
        }
    }
}
