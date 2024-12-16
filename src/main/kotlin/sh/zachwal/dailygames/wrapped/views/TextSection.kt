package sh.zachwal.dailygames.wrapped.views

import kotlinx.html.DIV
import kotlinx.html.div
import kotlinx.html.h1
import kotlinx.html.h3

data class TextSection(
    val topText: String,
    val middleText: String,
    val bottomText: String,
) : WrappedSection() {
    override fun DIV.content() {
        h3(classes = "top-text") {
            +topText
        }
        div(classes = "d-flex flex-row justify-content-center") {
            h1(classes = "animate-reveal") {
                +middleText
            }
        }
        h3(classes = "bottom-text") {
            +bottomText
        }
    }

    override val classes = "justify-content-between text-section"
}
