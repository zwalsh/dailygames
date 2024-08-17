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
}
