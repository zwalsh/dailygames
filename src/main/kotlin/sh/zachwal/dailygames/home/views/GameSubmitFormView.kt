package sh.zachwal.dailygames.home.views

import kotlinx.html.ButtonType
import kotlinx.html.DIV
import kotlinx.html.FormMethod.post
import kotlinx.html.button
import kotlinx.html.div
import kotlinx.html.form
import kotlinx.html.i
import kotlinx.html.id
import kotlinx.html.textArea
import sh.zachwal.dailygames.shared_html.HTMLView

data class GameSubmitFormView(
    val includeShareButton: Boolean,
) : HTMLView<DIV>() {
    override fun DIV.render() {
        form(method = post) {
            div(classes = "mb-3") {
                textArea(classes = "form-control bg-dark-subtle", rows = "5") {
                    id = SHARE_TEXT_ID
                    name = SHARE_TEXT_ID
                    placeholder = "e.g. #Worldle #123 (10.08.2024) 4/6 (100%)..."
                }
            }
            val justifyClass = if (includeShareButton) {
                "justify-content-between"
            } else {
                "justify-content-end"
            }
            div(classes = "d-flex $justifyClass") {
                if (includeShareButton) {
                    button(classes = "btn btn-secondary") {
                        id = "share-text-button"
                        type = ButtonType.button
                        i(classes = "bi bi-box-arrow-up") {}
                    }
                }
                button(classes = "btn btn-success") {
                    id = "submit-button"
                    i(classes = "bi bi-arrow-repeat me-2 d-none") {
                        id = "submit-spinner"
                    }
                    +"Submit"
                }
            }
        }
    }
}
