package sh.zachwal.dailygames.nav

import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.Test

class NavViewTest {

    @Test
    fun `NavView has three items`() {
        val view = NavView("zach", NavItem.HOME)

        assertThat(view.navItems).hasSize(3)
    }

    @Test
    fun `Items are Home, Leaderboard, and Profile in order`() {
        val items = NavView("zach", NavItem.HOME).navItems
        assertThat(items.first()).isInstanceOf(NavItemView::class.java)
        assertThat((items.first() as NavItemView).text).isEqualTo("Home")

        assertThat(items[1]).isInstanceOf(LeaderboardNavItemView::class.java)

        assertThat(items[2]).isInstanceOf(NavItemView::class.java)
        assertThat((items[2] as NavItemView).text).isEqualTo("Profile")
    }

    @Test
    fun `one active item matches passed currentActiveNavItem`() {
        val view = NavView("zach", NavItem.PROFILE)

        assertThat((view.navItems[2] as NavItemView).isActive).isTrue()
    }
}
