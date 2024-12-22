package sh.zachwal.dailygames.wrapped.views

import kotlinx.html.DIV
import kotlinx.html.div
import kotlinx.html.h1
import kotlinx.html.h3
import kotlinx.html.style

data class TextSection(
    val topText: String,
    val middleText: String,
    val bottomText: String,
    val fontSizeOverride: String? = null,
    val wrappedIndex: Int,
) : WrappedSection(wrappedIndex = wrappedIndex) {
    override fun DIV.content() {
        h3(classes = "top-text") {
            +topText
        }
        div(classes = "d-flex flex-row justify-content-center") {
            h1(classes = "animate animate-rev") {
                fontSizeOverride?.let { fs ->
                    style = "font-size: $fs;"
                }
                attributes["text"] = middleText
                +"..."
            }
        }
        h3(classes = "bottom-text") {
            +bottomText
        }
    }

    override val classes = "justify-content-between text-section"
}
