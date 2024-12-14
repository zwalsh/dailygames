package sh.zachwal.dailygames.wrapped.views

import kotlinx.html.DIV
import kotlinx.html.div
import kotlinx.html.h1
import kotlinx.html.h2
import kotlinx.html.h3
import sh.zachwal.dailygames.shared_html.HTMLView

data class WrappedSection(
    val x: String
): HTMLView<DIV>() {
    override fun DIV.render() {
        div(classes = "row vh-90 snapChild") {
            div(classes = "col card vw-100 mt-3 mx-3 p-3 d-flex justify-content-center bg-dark-subtle") {
                h1(classes = "year") {
                    +"2024"
                }
                h2 {
                    +"Hello, zach!"
                }
                h3 {
                    +"Welcome to your Daily Games Wrapped, 2024."
                }
            }
        }
    }
}
