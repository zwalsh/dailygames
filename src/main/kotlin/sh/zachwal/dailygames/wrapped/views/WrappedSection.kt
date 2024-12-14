package sh.zachwal.dailygames.wrapped.views

import kotlinx.html.DIV
import kotlinx.html.div
import sh.zachwal.dailygames.shared_html.HTMLView

abstract class WrappedSection : HTMLView<DIV>() {
    override fun DIV.render() {
        div(classes = "row vh-90 snapChild") {
            div(classes = "col card vw-100 mt-3 mx-3 p-3 d-flex justify-content-center bg-dark-subtle $classes") {
                content()
            }
        }
    }

    abstract fun DIV.content()

    open val classes: String = ""
}
