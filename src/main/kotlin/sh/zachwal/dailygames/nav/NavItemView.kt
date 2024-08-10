package sh.zachwal.dailygames.nav

import kotlinx.html.UL
import kotlinx.html.a
import kotlinx.html.i
import kotlinx.html.li
import sh.zachwal.dailygames.shared_html.HTMLView

data class NavItemView(
    val href: String,
    val icon: String,
    val text: String,
    val isActive: Boolean
) : HTMLView<UL>() {

    private val textStyling = if (isActive) {
        "text-white"
    } else {
        "text-secondary"
    }

    override fun UL.render() {
        li {
            a(href = href, classes = "nav-link $textStyling text-small") {
                i(classes = "bi $icon d-block text-center fs-3")
                +text
            }
        }
    }
}
