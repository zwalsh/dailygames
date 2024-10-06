package sh.zachwal.dailygames.nav

import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.Test

class NavViewTest {

    @Test
    fun `NavView has four items`() {
        val view = NavView(NavItem.HOME)

        assertThat(view.navItems).hasSize(4)
    }

    @Test
    fun `Items are Home, Chat, Leaderboard, and Profile in order`() {
        val items = NavView(NavItem.HOME).navItems
        assertThat(items.first()).isInstanceOf(NavItemView::class.java)
        assertThat((items.first() as NavItemView).text).isEqualTo("Home")

        assertThat(items[1]).isInstanceOf(ChatNavItemView::class.java)

        assertThat(items[2]).isInstanceOf(LeaderboardNavItemView::class.java)

        assertThat(items[3]).isInstanceOf(NavItemView::class.java)
        assertThat((items[3] as NavItemView).text).isEqualTo("Profile")
    }

    @Test
    fun `one active item matches passed currentActiveNavItem`() {
        val view = NavView(NavItem.PROFILE)

        assertThat((view.navItems[3] as NavItemView).isActive).isTrue()
    }
}
