package sh.zachwal.dailygames.leaderboard

import org.jdbi.v3.core.Handle
import org.jdbi.v3.core.Jdbi
import sh.zachwal.dailygames.db.dao.game.FlagleDAO
import sh.zachwal.dailygames.db.dao.game.PinpointDAO
import sh.zachwal.dailygames.db.dao.game.PuzzleResultDAO
import sh.zachwal.dailygames.db.dao.game.Top5DAO
import sh.zachwal.dailygames.db.dao.game.TradleDAO
import sh.zachwal.dailygames.db.dao.game.TravleDAO
import sh.zachwal.dailygames.db.dao.game.WorldleDAO
import sh.zachwal.dailygames.db.jdbi.User
import sh.zachwal.dailygames.db.jdbi.puzzle.Game
import sh.zachwal.dailygames.leaderboard.views.BasicScoreHintView
import sh.zachwal.dailygames.leaderboard.views.GameLeaderboardView
import sh.zachwal.dailygames.leaderboard.views.LeaderboardView
import sh.zachwal.dailygames.leaderboard.views.TravleScoreHintView
import sh.zachwal.dailygames.users.UserService
import java.time.Instant
import java.time.temporal.ChronoUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LeaderboardService @Inject constructor(
    private val userService: UserService,
    private val jdbi: Jdbi,
    private val pointCalculator: PuzzleResultPointCalculator,
) {

    fun overallLeaderboardView(currentUser: User): LeaderboardView {
        return LeaderboardView(currentUser.username)
    }

    fun gameLeaderboardView(currentUser: User, game: Game): GameLeaderboardView {
        val scoreHintView = when (game) {
            Game.WORLDLE -> BasicScoreHintView("Scoring: 1 point for the correct answer, 1 point per guess left. e.g. 2/6 = 5 points.")
            Game.TRADLE -> BasicScoreHintView("Scoring: 1 point for the correct answer, 1 point per guess left. e.g. 2/6 = 5 points.")
            Game.TRAVLE -> TravleScoreHintView()
            Game.TOP5 -> BasicScoreHintView("Scoring: One point per correct guess. One point per life left if all 5 answers guessed correctly.")
            Game.FLAGLE -> BasicScoreHintView("Scoring: 1 point for the correct answer, 1 point per guess left. e.g. 2/6 = 5 points.")
            Game.PINPOINT -> BasicScoreHintView("Scoring: 1 point for the correct answer, 1 point per guess left. e.g. 2/5 = 4 points.")
        }
        return GameLeaderboardView(username = currentUser.username, game = game, scoreHintView = scoreHintView)
    }

    data class TotalPoints(val games: Int, val totalPoints: Int) {
        fun averagePoints(): Double {
            return totalPoints.toDouble() / games
        }

        fun addPerformance(points: TotalPoints): TotalPoints {
            return TotalPoints(games + points.games, totalPoints + points.totalPoints)
        }
    }

    fun gameLeaderboardData(currentUser: User, game: Game): LeaderboardData {
        val allTimeAverageScoreByUserId = mutableMapOf<Long, TotalPoints>()
        val past30DaysAverageScoreByUserId = mutableMapOf<Long, TotalPoints>()
        jdbi.open().use { handle ->
            val dao = daoForGame(game, handle)
            dao.allResultsStream().forEach { result ->
                val totalPoints = TotalPoints(1, pointCalculator.calculatePoints(result))
                allTimeAverageScoreByUserId.merge(result.userId, totalPoints, TotalPoints::addPerformance)
                if (result.instantSubmitted.isAfter(Instant.now().minus(30, ChronoUnit.DAYS))) {
                    past30DaysAverageScoreByUserId.merge(result.userId, totalPoints, TotalPoints::addPerformance)
                }
            }
        }

        return LeaderboardData(
            allTime = chartInfoFromAverageScores(game, allTimeAverageScoreByUserId),
            past30Days = chartInfoFromAverageScores(game, past30DaysAverageScoreByUserId),
        )
    }

    private fun chartInfoFromAverageScores(game: Game, scores: Map<Long, TotalPoints>): ChartInfo {
        val sortedScores = scores.entries.sortedByDescending { it.value.averagePoints() }.take(5)
        val labels = sortedScores.map { userService.getUser(it.key)?.username ?: "Unknown" }
        val dataPoints = sortedScores.map { it.value.averagePoints() }
        return ChartInfo(labels, dataPoints)
    }

    private fun daoForGame(game: Game, handle: Handle): PuzzleResultDAO<*> {
        return when (game) {
            Game.WORLDLE -> handle.attach(WorldleDAO::class.java)
            Game.TRADLE -> handle.attach(TradleDAO::class.java)
            Game.TRAVLE -> handle.attach(TravleDAO::class.java)
            Game.TOP5 -> handle.attach(Top5DAO::class.java)
            Game.FLAGLE -> handle.attach(FlagleDAO::class.java)
            Game.PINPOINT -> handle.attach(PinpointDAO::class.java)
        }
    }
}
