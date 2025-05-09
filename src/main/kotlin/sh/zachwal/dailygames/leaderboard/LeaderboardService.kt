package sh.zachwal.dailygames.leaderboard

import org.jdbi.v3.core.Jdbi
import org.jdbi.v3.sqlobject.kotlin.attach
import sh.zachwal.dailygames.db.dao.game.PuzzleResultDAO
import sh.zachwal.dailygames.db.jdbi.User
import sh.zachwal.dailygames.db.jdbi.puzzle.Game
import sh.zachwal.dailygames.leaderboard.responses.ChartInfo
import sh.zachwal.dailygames.leaderboard.responses.LeaderboardData
import sh.zachwal.dailygames.leaderboard.views.BasicScoreHintView
import sh.zachwal.dailygames.leaderboard.views.GameLeaderboardView
import sh.zachwal.dailygames.leaderboard.views.LeaderboardView
import sh.zachwal.dailygames.leaderboard.views.TravleScoreHintView
import sh.zachwal.dailygames.nav.NavItem
import sh.zachwal.dailygames.nav.NavViewFactory
import sh.zachwal.dailygames.users.UserPreferencesService
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
    private val userPreferencesService: UserPreferencesService,
    private val jdbi: Jdbi,
    private val pointCalculator: PointCalculator,
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
            Game.WORLDLE,
            Game.TRADLE,
            Game.FLAGLE,
            Game.FRAMED,
            Game.BANDLE -> BasicScoreHintView("Scoring: 1 point for the correct answer, 1 point per guess left. e.g. 2/6 = 5 points.")

            Game.PINPOINT -> BasicScoreHintView("Scoring: 1 point for the correct answer, 1 point per guess left. e.g. 2/5 = 4 points.")
            Game.GEOGRID -> BasicScoreHintView("Scoring: 1 point per correct guess. e.g. 8/9 = 8 points.")

            Game.TOP5 -> BasicScoreHintView("Scoring: One point per correct guess. One point per life left if all 5 answers guessed correctly.")
            Game.GEOCIRCLES -> BasicScoreHintView("Scoring: 1 point for each green circle, 1 point for each life left.")

            Game.TRAVLE -> TravleScoreHintView()

            Game.BRACKET_CITY -> BasicScoreHintView("Scoring: Bracket City score divided by 10 and rounded down. e.g. 99.0 = 9 points.")
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
        val overallPointsData = Game.values().map { game ->
            pointsDataForGame(game)
        }.fold(PointsData(emptyMap(), emptyMap(), emptyMap())) { acc, pointsPerUser ->
            acc.merge(pointsPerUser)
        }

        return leaderboardData(overallPointsData)
    }

    fun gameLeaderboardData(game: Game): LeaderboardData {
        val pointsPerUser = pointsDataForGame(game)

        return leaderboardData(pointsPerUser)
    }

    private fun leaderboardData(
        pointsData: PointsData
    ) = LeaderboardData(
        allTimePoints = chartInfo(pointsData.allTimePerUser) { it.totalPoints.toDouble() },
        allTimeGames = chartInfo(pointsData.allTimePerUser) { it.games.toDouble() },
        allTimeAverage = chartInfo(pointsData.allTimePerUser.filterValues { it.games >= minimumGamesForAverage }) { it.averagePoints() },
        thirtyDaysPoints = chartInfo(pointsData.thirtyDaysPerUser) { it.totalPoints.toDouble() },
        thirtyDaysGames = chartInfo(pointsData.thirtyDaysPerUser) { it.games.toDouble() },
        thirtyDaysAverage = chartInfo(pointsData.thirtyDaysPerUser.filterValues { it.games >= minimumGamesForAverage }) { it.averagePoints() },
        pointsHistogram = histogramChartInfo(pointsData.pointsHistogram),
    )

    private fun pointsDataForGame(game: Game): PointsData {
        val allTimeTotalsPerUser = mutableMapOf<Long, TotalPoints>()
        val thirtyDaysTotalsPerUser = mutableMapOf<Long, TotalPoints>()
        val gameCountByPoints = mutableMapOf<Int, Int>()
        jdbi.open().use { handle ->
            val dao = handle.attach<PuzzleResultDAO>()
            dao.allResultsForGameStream(game).forEach { result ->
                val points = pointCalculator.calculatePoints(result)
                val totalPoints = TotalPoints(1, points)
                allTimeTotalsPerUser.merge(result.userId, totalPoints, TotalPoints::addPerformance)
                if (result.instantSubmitted.isAfter(Instant.now().minus(30, ChronoUnit.DAYS))) {
                    thirtyDaysTotalsPerUser.merge(result.userId, totalPoints, TotalPoints::addPerformance)
                }
                gameCountByPoints.merge(points, 1, Int::plus)
            }
        }
        return PointsData(
            allTimePerUser = allTimeTotalsPerUser,
            thirtyDaysPerUser = thirtyDaysTotalsPerUser,
            pointsHistogram = gameCountByPoints,
        )
    }

    private fun chartInfo(scores: Map<Long, TotalPoints>, selector: (TotalPoints) -> Double): ChartInfo {
        val sortedScores = scores.entries.sortedByDescending { selector(it.value) }.take(5)
        val labels = sortedScores.map { userService.getUser(it.key)?.username ?: "Unknown" }
        val dataPoints = sortedScores.map { selector(it.value) }
        return ChartInfo(labels, dataPoints)
    }

    private fun histogramChartInfo(scores: Map<Int, Int>): ChartInfo {
        val totalGamesPlayed = scores.values.sum()
        val sortedDataPoints = scores.entries.sortedByDescending { it.key }
        val labels = sortedDataPoints.map { it.key.toString() }
        val dataPoints = sortedDataPoints
            .map { it.value.toDouble() / totalGamesPlayed } // calculate percentage
            .map { it * 100 } // convert to percentage
            .map { String.format("%.1f", it).toDouble() } // round to 1 decimal place
        return ChartInfo(labels, dataPoints)
    }

    /**
     * Returns today's top five scorers by points across all games, sorted by score as a ChartInfo object.
     *
     * "Today" is based on the given user's timezone.
     */
    fun dailyLeaderboardData(userId: Long): ChartInfo {
        val userTimeZone = userPreferencesService.getTimeZoneCached(userId)
        val startOfToday = Instant.now().atZone(userTimeZone).truncatedTo(ChronoUnit.DAYS).toInstant()
        val endOfToday = startOfToday.plus(1, ChronoUnit.DAYS)

        return jdbi.open().use { handle ->
            val dao = handle.attach<PuzzleResultDAO>()
            val pointsByUser = mutableMapOf<Long, Int>()

            dao.allResultsBetweenStream(startOfToday, endOfToday)
                .forEach { result ->
                    val points = pointCalculator.calculatePoints(result)
                    pointsByUser[result.userId] = pointsByUser.getOrDefault(result.userId, 0) + points
                }

            val sortedDataPoints = pointsByUser
                .entries
                .sortedByDescending { it.value }
                .take(5)
            val labels = sortedDataPoints.map { userService.getUsernameCached(it.key) ?: "Unknown" }
            val dataPoints = sortedDataPoints.map { it.value.toDouble() }
            ChartInfo(labels, dataPoints)
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

data class PointsData constructor(
    val allTimePerUser: Map<Long, TotalPoints>,
    val thirtyDaysPerUser: Map<Long, TotalPoints>,
    val pointsHistogram: Map<Int, Int>,
) {
    fun merge(other: PointsData): PointsData {
        val newAllTime = (allTimePerUser.keys + other.allTimePerUser.keys).associateWith { userId ->
            allTimePerUser
                .getOrDefault(userId, TotalPoints(0, 0))
                .addPerformance(
                    other.allTimePerUser.getOrDefault(userId, TotalPoints(0, 0))
                )
        }
        val newThirtyDays = (thirtyDaysPerUser.keys + other.thirtyDaysPerUser.keys).associateWith { userId ->
            thirtyDaysPerUser
                .getOrDefault(userId, TotalPoints(0, 0))
                .addPerformance(other.thirtyDaysPerUser.getOrDefault(userId, TotalPoints(0, 0)))
        }

        val newHistogram = (pointsHistogram.keys + other.pointsHistogram.keys).associateWith { points ->
            pointsHistogram.getOrDefault(points, 0) + other.pointsHistogram.getOrDefault(points, 0)
        }

        return PointsData(
            allTimePerUser = newAllTime,
            thirtyDaysPerUser = newThirtyDays,
            pointsHistogram = newHistogram,
        )
    }
}
