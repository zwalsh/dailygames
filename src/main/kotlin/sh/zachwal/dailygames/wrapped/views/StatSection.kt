package sh.zachwal.dailygames.wrapped.views

import kotlinx.html.DIV
import kotlinx.html.div
import kotlinx.html.h1
import kotlinx.html.h3

data class StatSection(
    val topText: String,
    val stat: Int,
    val bottomText: String,
    val wrappedIndex: Int,
) : WrappedSection(wrappedIndex = wrappedIndex) {
    override fun DIV.content() {
        h3(classes = "top-text") {
            +topText
        }
        div(classes = "d-flex flex-row justify-content-center") {
            h1(classes = "animate animate-count-up") {
                attributes["data-count"] = stat.toString()
                +"0"
            }
        }
        h3(classes = "bottom-text") {
            +bottomText
        }
    }

    override val classes = "justify-content-between count-section"
}
