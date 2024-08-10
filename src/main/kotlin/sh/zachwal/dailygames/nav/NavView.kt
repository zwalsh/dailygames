package sh.zachwal.dailygames.nav

import kotlinx.html.BODY
import kotlinx.html.UL
import kotlinx.html.a
import kotlinx.html.div
import kotlinx.html.header
import kotlinx.html.i
import kotlinx.html.li
import kotlinx.html.span
import kotlinx.html.ul
import sh.zachwal.dailygames.shared_html.HTMLView

enum class NavItem {
    HOME,
    LEADERBOARD,
    PROFILE,
}


data class NavView constructor(
    val username: String,
    val currentActiveNavItem: NavItem,
) : HTMLView<BODY>() {
    override fun BODY.render() {
        header(classes = "px-3 py-3 mb-4 border-bottom") {
            div(classes = "container") {
                div(classes = "d-flex flex-wrap align-items-center justify-content-center") {
                    a(href = "/", classes = "nav-link") {
                        i(classes = "bi bi-globe-europe-africa fs-1 m-2")
                        span(classes = "lead align-text-bottom") { +"Daily Games" }
                    }
                    ul(classes = "nav justify-content-center my-md-0 text-small") {
                        navItem(
                            href = "/",
                            icon = "bi-house-door-fill",
                            isActive = currentActiveNavItem == NavItem.HOME,
                            text = "Home"
                        )
                        navItem(
                            href = "/leaderboard",
                            icon = "bi-bar-chart-fill",
                            isActive = currentActiveNavItem == NavItem.LEADERBOARD,
                            text = "Leaderboard"
                        )
                        // TODO move to /profile/$username
                        navItem(
                            href = "/profile",
                            icon = "bi-person-circle",
                            isActive = currentActiveNavItem == NavItem.PROFILE,
                            text = "Profile"
                        )
                    }
                }
            }
        }
    }

    private fun UL.navItem(href: String, icon: String, text: String, isActive: Boolean) {
        val textStyling = if (isActive) {
            "text-white"
        } else {
            "text-secondary"
        }
        li {
            a(href = href, classes = "nav-link $textStyling text-small") {
                i(classes = "bi $icon d-block text-center fs-3")
                +text
            }
        }
    }
}