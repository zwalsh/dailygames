package sh.zachwal.dailygames.wrapped.views

import kotlinx.html.DIV
import kotlinx.html.h1
import kotlinx.html.h2
import kotlinx.html.h3

data class WelcomeSection(
    val year: Int,
    val name: String,
) : WrappedSection() {
    override fun DIV.content() {
        h1 {
            +year.toString()
        }
        h2 {
            +"Hello, $name!"
        }
        h3 {
            +"Welcome to your Daily Games Wrapped, $year."
        }
    }

    override val classes = "welcome"
}
