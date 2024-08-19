package sh.zachwal.dailygames.leaderboard

import sh.zachwal.dailygames.db.jdbi.User
import sh.zachwal.dailygames.db.jdbi.puzzle.Game
import sh.zachwal.dailygames.leaderboard.views.GameLeaderboardView
import sh.zachwal.dailygames.leaderboard.views.LeaderboardView

class LeaderboardService {

    fun overallLeaderboardView(currentUser: User): LeaderboardView {
        return LeaderboardView(currentUser.username)
    }

    fun gameLeaderboardView(currentUser: User, game: Game): GameLeaderboardView {
        return GameLeaderboardView(username = currentUser.username, game = game)
    }

    fun gameLeaderboardData(currentUser: User, game: Game): LeaderboardData {

        // copy the hardcoded data from leaderboard.js
        return LeaderboardData(
            allTime = ChartInfo(
                labels = listOf("zach", "derknasty", "jackiewalsh", "ChatGPT", "MikMap"),
                dataPoints = listOf(5.5, 5.4, 5.3, 5.2, 5.1)
            ),
            past30Days = ChartInfo(
                labels = listOf("zach", "derknasty", "jackiewalsh", "ChatGPT", "MikMap"),
                dataPoints = listOf(5.7, 5.4, 5.3, 4.8, 4.7)
            )
        )
    }
}
