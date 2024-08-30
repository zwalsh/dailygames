package sh.zachwal.dailygames.nav

import kotlinx.html.BODY
import kotlinx.html.UL
import kotlinx.html.div
import kotlinx.html.header
import kotlinx.html.ul
import sh.zachwal.dailygames.shared_html.HTMLView

enum class NavItem {
    HOME,
    CHAT,
    LEADERBOARD,
    PROFILE,
}

data class NavView constructor(
    val navItems: List<HTMLView<UL>>
) : HTMLView<BODY>() {

    constructor(
        username: String,
        currentActiveNavItem: NavItem,
    ) : this(
        listOf(
            NavItemView(
                href = "/",
                icon = "bi-house-door-fill",
                isActive = currentActiveNavItem == NavItem.HOME,
                text = "Home"
            ),
            ChatNavItemView(
                isActive = currentActiveNavItem == NavItem.CHAT,
            ),
            LeaderboardNavItemView(
                isActive = currentActiveNavItem == NavItem.LEADERBOARD,
            ),
            NavItemView(
                href = "/profile",
                icon = "bi-person-circle",
                isActive = currentActiveNavItem == NavItem.PROFILE,
                text = "Profile"
            )
        )
    )

    override fun BODY.render() {
        header(classes = "py-1 mb-4 border-bottom bg-dark-subtle") {
            div(classes = "container") {
                div(classes = "d-flex flex-wrap align-items-center justify-content-center") {
                    ul(classes = "nav justify-content-center my-md-0 text-small") {
                        navItems.forEach {
                            it.renderIn(this)
                        }
                    }
                }
            }
        }
    }
}
