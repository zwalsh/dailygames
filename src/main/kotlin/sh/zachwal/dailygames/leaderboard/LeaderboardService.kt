package sh.zachwal.dailygames.leaderboard

import org.jdbi.v3.core.Handle
import org.jdbi.v3.core.Jdbi
import sh.zachwal.dailygames.db.dao.game.FlagleDAO
import sh.zachwal.dailygames.db.dao.game.GeocirclesDAO
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
import sh.zachwal.dailygames.nav.NavItem
import sh.zachwal.dailygames.nav.NavViewFactory
import sh.zachwal.dailygames.users.UserService
import java.time.Instant
import java.time.temporal.ChronoUnit
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton

const val MINIMUM_GAMES_FOR_AVERAGE = 10

@Singleton
class LeaderboardService @Inject constructor(
    private val userService: UserService,
    private val jdbi: Jdbi,
    private val pointCalculator: PuzzleResultPointCalculator,
    private val navViewFactory: NavViewFactory,
    @Named("leaderboardMinimumGamesForAverage")
    private val minimumGamesForAverage: Int,
) {

    fun overallLeaderboardView(currentUser: User): LeaderboardView {
        val nav = navViewFactory.navView(
            username = currentUser.username,
            currentActiveNavItem = NavItem.LEADERBOARD,
        )
        return LeaderboardView(nav = nav)
    }

    fun gameLeaderboardView(currentUser: User, game: Game): GameLeaderboardView {
        val scoreHintView = when (game) {
            Game.WORLDLE -> BasicScoreHintView("Scoring: 1 point for the correct answer, 1 point per guess left. e.g. 2/6 = 5 points.")
            Game.TRADLE -> BasicScoreHintView("Scoring: 1 point for the correct answer, 1 point per guess left. e.g. 2/6 = 5 points.")
            Game.TRAVLE -> TravleScoreHintView()
            Game.TOP5 -> BasicScoreHintView("Scoring: One point per correct guess. One point per life left if all 5 answers guessed correctly.")
            Game.FLAGLE -> BasicScoreHintView("Scoring: 1 point for the correct answer, 1 point per guess left. e.g. 2/6 = 5 points.")
            Game.PINPOINT -> BasicScoreHintView("Scoring: 1 point for the correct answer, 1 point per guess left. e.g. 2/5 = 4 points.")
            Game.GEOCIRCLES -> BasicScoreHintView("Scoring: 1 point for each green circle, 1 point for each life left.")
        }
        val navView = navViewFactory.navView(
            username = currentUser.username,
            currentActiveNavItem = NavItem.LEADERBOARD,
        )
        return GameLeaderboardView(
            game = game,
            scoreHintView = scoreHintView,
            nav = navView,
        )
    }

    fun overallLeaderboardData(): LeaderboardData {
        val overallPointsPerUser = Game.values().map { game ->
            totalPointsPerUserOnGame(game)
        }.fold(PointsPerUser(emptyMap(), emptyMap())) { acc, pointsPerUser ->
            acc.merge(pointsPerUser)
        }

        return leaderboardData(overallPointsPerUser)
    }

    fun gameLeaderboardData(game: Game): LeaderboardData {
        val pointsPerUser = totalPointsPerUserOnGame(game)

        return leaderboardData(pointsPerUser)
    }

    private fun leaderboardData(
        pointsPerUser: PointsPerUser
    ) = LeaderboardData(
        allTimePoints = chartInfo(pointsPerUser.allTime) { it.totalPoints.toDouble() },
        allTimeGames = chartInfo(pointsPerUser.allTime) { it.games.toDouble() },
        allTimeAverage = chartInfo(pointsPerUser.allTime.filterValues { it.games >= minimumGamesForAverage }) { it.averagePoints() },
        thirtyDaysPoints = chartInfo(pointsPerUser.thirtyDays) { it.totalPoints.toDouble() },
        thirtyDaysGames = chartInfo(pointsPerUser.thirtyDays) { it.games.toDouble() },
        thirtyDaysAverage = chartInfo(pointsPerUser.thirtyDays.filterValues { it.games >= minimumGamesForAverage }) { it.averagePoints() },
    )

    private fun totalPointsPerUserOnGame(game: Game): PointsPerUser {
        val allTimeTotalsPerUser = mutableMapOf<Long, TotalPoints>()
        val thirtyDaysTotalsPerUser = mutableMapOf<Long, TotalPoints>()
        jdbi.open().use { handle ->
            val dao = daoForGame(game, handle)
            dao.allResultsStream().forEach { result ->
                val totalPoints = TotalPoints(1, pointCalculator.calculatePoints(result))
                allTimeTotalsPerUser.merge(result.userId, totalPoints, TotalPoints::addPerformance)
                if (result.instantSubmitted.isAfter(Instant.now().minus(30, ChronoUnit.DAYS))) {
                    thirtyDaysTotalsPerUser.merge(result.userId, totalPoints, TotalPoints::addPerformance)
                }
            }
        }
        return PointsPerUser(allTimeTotalsPerUser, thirtyDaysTotalsPerUser)
    }

    private fun chartInfo(scores: Map<Long, TotalPoints>, selector: (TotalPoints) -> Double): ChartInfo {
        val sortedScores = scores.entries.sortedByDescending { selector(it.value) }.take(5)
        val labels = sortedScores.map { userService.getUser(it.key)?.username ?: "Unknown" }
        val dataPoints = sortedScores.map { selector(it.value) }
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
            Game.GEOCIRCLES -> handle.attach(GeocirclesDAO::class.java)
        }
    }
}

data class TotalPoints(val games: Int, val totalPoints: Int) {
    fun averagePoints(): Double {
        return totalPoints.toDouble() / games
    }

    fun addPerformance(points: TotalPoints): TotalPoints {
        return TotalPoints(games + points.games, totalPoints + points.totalPoints)
    }
}

data class PointsPerUser(
    val allTime: Map<Long, TotalPoints>,
    val thirtyDays: Map<Long, TotalPoints>,
) {
    fun merge(other: PointsPerUser): PointsPerUser {
        val newAllTime = (allTime.keys + other.allTime.keys).associateWith { userId ->
            allTime
                .getOrDefault(userId, TotalPoints(0, 0))
                .addPerformance(
                    other.allTime.getOrDefault(userId, TotalPoints(0, 0))
                )
        }
        val newThirtyDays = (thirtyDays.keys + other.thirtyDays.keys).associateWith { userId ->
            thirtyDays
                .getOrDefault(userId, TotalPoints(0, 0))
                .addPerformance(other.thirtyDays.getOrDefault(userId, TotalPoints(0, 0)))
        }

        return PointsPerUser(
            allTime = newAllTime,
            thirtyDays = newThirtyDays,
        )
    }
}
