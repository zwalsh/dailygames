package sh.zachwal.dailygames.wrapped.views

import kotlinx.html.DIV
import kotlinx.html.div
import kotlinx.html.h1
import kotlinx.html.h3

data class StatSection(
    val topText: String,
    val bottomText: String,
    val stat: Int,
) : WrappedSection() {
    override fun DIV.content() {
        h3(classes = "top-text") {
            +topText
        }
        div(classes = "d-flex flex-row justify-content-center") {
            h1(classes = "animate-count-up d-none") {
                +stat.toString()
            }
        }
        h3(classes = "bottom-text") {
            +bottomText
        }
    }

    override val classes = "justify-content-between count"
}
