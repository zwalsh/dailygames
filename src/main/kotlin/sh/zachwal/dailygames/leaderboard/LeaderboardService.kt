package sh.zachwal.dailygames.leaderboard

import org.jdbi.v3.core.Handle
import org.jdbi.v3.core.Jdbi
import sh.zachwal.dailygames.db.dao.game.FlagleDAO
import sh.zachwal.dailygames.db.dao.game.PuzzleResultDAO
import sh.zachwal.dailygames.db.dao.game.Top5DAO
import sh.zachwal.dailygames.db.dao.game.TradleDAO
import sh.zachwal.dailygames.db.dao.game.TravleDAO
import sh.zachwal.dailygames.db.dao.game.WorldleDAO
import sh.zachwal.dailygames.db.jdbi.User
import sh.zachwal.dailygames.db.jdbi.puzzle.Game
import sh.zachwal.dailygames.leaderboard.views.GameLeaderboardView
import sh.zachwal.dailygames.leaderboard.views.LeaderboardView
import sh.zachwal.dailygames.users.UserService
import java.time.Instant
import java.time.temporal.ChronoUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LeaderboardService @Inject constructor(
    private val userService: UserService,
    private val jdbi: Jdbi
) {

    fun overallLeaderboardView(currentUser: User): LeaderboardView {
        return LeaderboardView(currentUser.username)
    }

    fun gameLeaderboardView(currentUser: User, game: Game): GameLeaderboardView {
        val scoringText = when (game) {
            Game.WORLDLE -> "Scoring: Number of guesses needed out of six. X/6 = 7."
            Game.TRADLE -> "Scoring: Number of guesses needed out of six. X/6 = 7."
            Game.TRAVLE -> "Scoring: Excess guesses needed to solve the puzzle. 0 is perfect."
            Game.TOP5 -> "Scoring: One point per correct guess. One point per life left if all 5 answers guessed correctly."
            Game.FLAGLE -> "Scoring: Number of guesses needed out of six. X/6 = 7."
        }
        return GameLeaderboardView(username = currentUser.username, game = game, scoringText = scoringText)
    }

    data class TotalScore(val games: Int, val totalScore: Int) {
        fun averageScore(): Double {
            return totalScore.toDouble() / games
        }

        fun addPerformance(score: TotalScore): TotalScore {
            return TotalScore(games + score.games, totalScore + score.totalScore)
        }
    }

    fun gameLeaderboardData(currentUser: User, game: Game): LeaderboardData {
        val allTimeAverageScoreByUserId = mutableMapOf<Long, TotalScore>()
        val past30DaysAverageScoreByUserId = mutableMapOf<Long, TotalScore>()
        jdbi.open().use { handle ->
            val dao = daoForGame(game, handle)
            dao.allResultsStream().forEach { result ->
                val totalScore = TotalScore(1, result.score)
                allTimeAverageScoreByUserId.merge(result.userId, totalScore, TotalScore::addPerformance)
                if (result.instantSubmitted.isAfter(Instant.now().minus(30, ChronoUnit.DAYS))) {
                    past30DaysAverageScoreByUserId.merge(result.userId, totalScore, TotalScore::addPerformance)
                }
            }
        }

        return LeaderboardData(
            allTime = chartInfoFromAverageScores(allTimeAverageScoreByUserId),
            past30Days = chartInfoFromAverageScores(past30DaysAverageScoreByUserId),
        )
    }

    private fun chartInfoFromAverageScores(scores: Map<Long, TotalScore>): ChartInfo {
        val sortedScores = scores.entries.sortedByDescending { it.value.averageScore() }.take(5)
        val labels = sortedScores.map { userService.getUser(it.key)?.username ?: "Unknown" }
        val dataPoints = sortedScores.map { it.value.averageScore() }
        return ChartInfo(labels, dataPoints)
    }

    private fun daoForGame(game: Game, handle: Handle): PuzzleResultDAO<*> {
        return when (game) {
            Game.WORLDLE -> handle.attach(WorldleDAO::class.java)
            Game.TRADLE -> handle.attach(TradleDAO::class.java)
            Game.TRAVLE -> handle.attach(TravleDAO::class.java)
            Game.TOP5 -> handle.attach(Top5DAO::class.java)
            Game.FLAGLE -> handle.attach(FlagleDAO::class.java)
        }
    }
}
