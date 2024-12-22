package sh.zachwal.dailygames.wrapped.views

import kotlinx.html.DIV
import kotlinx.html.h1
import kotlinx.html.h2

data class GuestWelcomeSection(
    val year: Int,
    val name: String,
    val wrappedIndex: Int,
) : WrappedSection(wrappedIndex = wrappedIndex) {
    override fun DIV.content() {
        h1 {
            +year.toString()
        }
        h2 {
            +"Check out $name's Daily Games Wrapped, $year."
        }
    }

    override val classes = "justify-content-center welcome-section"
}
