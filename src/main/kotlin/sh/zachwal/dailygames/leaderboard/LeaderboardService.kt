package sh.zachwal.dailygames.leaderboard

import sh.zachwal.dailygames.db.jdbi.User
import sh.zachwal.dailygames.leaderboard.views.LeaderboardView

class LeaderboardService {

    fun leaderboardView(currentUser: User): LeaderboardView {
        return LeaderboardView(currentUser.username)
    }
}
