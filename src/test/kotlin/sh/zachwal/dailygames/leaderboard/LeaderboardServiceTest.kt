package sh.zachwal.dailygames.leaderboard

import com.google.common.truth.Truth.assertThat
import sh.zachwal.dailygames.db.jdbi.User
import sh.zachwal.dailygames.nav.LeaderboardNavItemView
import kotlin.test.Test

class LeaderboardServiceTest {

    private val leaderboardService = LeaderboardService()

    @Test
    fun `leaderboardView returns LeaderboardView with Nav set to LEADERBOARD`() {
        val currentUser = User(id = 1L, username = "test", hashedPassword = "test")

        val leaderboardView = leaderboardService.leaderboardView(currentUser)

        val navItem = leaderboardView.nav.navItems[1]

        assertThat(navItem).isInstanceOf(LeaderboardNavItemView::class.java)

        val leaderboardNavItemView = navItem as LeaderboardNavItemView
        assertThat(leaderboardNavItemView.isActive).isTrue()
    }
}
