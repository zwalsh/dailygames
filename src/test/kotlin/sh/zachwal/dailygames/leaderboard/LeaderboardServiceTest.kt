package sh.zachwal.dailygames.leaderboard

import com.google.common.truth.Truth.assertThat
import sh.zachwal.dailygames.db.jdbi.User
import kotlin.test.Test

class LeaderboardServiceTest {

    private val leaderboardService = LeaderboardService()

    @Test
    fun `leaderboardView returns LeaderboardView with Nav set to LEADERBOARD`() {
        val currentUser = User(id = 1L, username = "test", hashedPassword = "test")

        val leaderboardView = leaderboardService.leaderboardView(currentUser)

        val leaderboardNavItem = leaderboardView.nav.navItems[1]

        assertThat(leaderboardNavItem.text).isEqualTo("Leaderboard")
        assertThat(leaderboardNavItem.isActive).isTrue()
    }
}
