package sh.zachwal.dailygames.wrapped.views

import kotlinx.html.DIV
import kotlinx.html.a
import kotlinx.html.div
import kotlinx.html.id
import sh.zachwal.dailygames.shared_html.HTMLView

abstract class WrappedSection(
    val height: String = "vh-90",
    private val wrappedIndex: Int,
) : HTMLView<DIV>() {
    override fun DIV.render() {
        a(classes = "link-unstyled", href = "#wrapped-section-${wrappedIndex + 1}") {
            div(classes = "row $height snapChild") {
                id = "wrapped-section-$wrappedIndex"

                div(classes = "col card vw-100 mt-3 mx-3 p-3 d-flex bg-dark-subtle $classes") {
                    content()
                }
            }
        }
    }

    abstract fun DIV.content()

    open val classes: String = ""
}
