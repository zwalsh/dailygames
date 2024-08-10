package sh.zachwal.dailygames.nav

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertIterableEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class NavViewTest {

    @Test
    fun `NavView has three items`() {
        val view = NavView("zach", NavItem.HOME)

        assertEquals(3, view.navItems.size)
    }

    @Test
    fun `Items are Home, Leaderboard, and Profile in order`() {
        val view = NavView("zach", NavItem.HOME)
        val itemNames = view.navItems.map { it.text }

        assertIterableEquals(
            listOf("Home", "Leaderboard", "Profile"),
            itemNames
        )
    }

    @Test
    fun `one active item matches passed currentActiveNavItem`() {
        val view = NavView("zach", NavItem.PROFILE)
        val actives = view.navItems.associate { it.text to it.isActive }

        assertTrue(actives["Profile"]!!)
        assertEquals(1, actives.values.count { it })
    }
}
