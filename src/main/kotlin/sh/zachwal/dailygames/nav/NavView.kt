package sh.zachwal.dailygames.nav

import kotlinx.html.BODY
import kotlinx.html.HEADER
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
    val navItems: List<HTMLView<UL>>,
    val insideNavItem: HTMLView<HEADER>? = null
) : HTMLView<BODY>() {

    constructor(
        username: String,
        currentActiveNavItem: NavItem,
        insideNavItem: HTMLView<HEADER>? = null,
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
                chatCounts = emptyMap(),
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
        ),
        insideNavItem
    )

    override fun BODY.render() {
        header(classes = "py-1 mb-4 border-bottom bg-dark-subtle sticky-top") {
            div(classes = "container") {
                div(classes = "d-flex flex-wrap align-items-center justify-content-center") {
                    ul(classes = "nav justify-content-center my-md-0 text-small") {
                        navItems.forEach {
                            it.renderIn(this)
                        }
                    }
                }
            }
            insideNavItem?.renderIn(this)
        }
    }
}
