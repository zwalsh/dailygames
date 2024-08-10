package sh.zachwal.dailygames.shared_html

import kotlinx.html.BODY

fun BODY.darkMode() {
    attributes["data-bs-theme"] = "dark"
}
