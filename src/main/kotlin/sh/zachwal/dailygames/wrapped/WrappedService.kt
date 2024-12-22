package sh.zachwal.dailygames.wrapped

import org.jdbi.v3.core.Jdbi
import org.jdbi.v3.sqlobject.kotlin.attach
import org.slf4j.LoggerFactory
import sh.zachwal.dailygames.db.dao.game.PuzzleResultDAO
import sh.zachwal.dailygames.db.jdbi.WrappedInfo
import sh.zachwal.dailygames.db.jdbi.puzzle.Game
import sh.zachwal.dailygames.leaderboard.MINIMUM_GAMES_FOR_AVERAGE
import sh.zachwal.dailygames.leaderboard.PointCalculator
import sh.zachwal.dailygames.users.UserPreferencesService
import sh.zachwal.dailygames.users.UserService
import sh.zachwal.dailygames.wrapped.views.RanksTableRowView
import sh.zachwal.dailygames.wrapped.views.RanksTableSection
import sh.zachwal.dailygames.wrapped.views.StatSection
import sh.zachwal.dailygames.wrapped.views.SummaryTableSection
import sh.zachwal.dailygames.wrapped.views.TextSection
import sh.zachwal.dailygames.wrapped.views.WelcomeSection
import sh.zachwal.dailygames.wrapped.views.WrappedView
import java.time.Duration
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WrappedService @Inject constructor(
    private val jdbi: Jdbi,
    private val calculator: PointCalculator,
    private val userService: UserService,
    private val userPreferencesService: UserPreferencesService,
) {

    private val logger = LoggerFactory.getLogger(WrappedService::class.java)

    private val bestDayFormatter = DateTimeFormatter.ofPattern("MMMM d")

    fun wrappedView(year: Int, userId: Long): WrappedView {
        val wrappedData = generateWrappedData(year)
        val wrappedInfo = wrappedData.firstOrNull { it.userId == userId }
            ?: throw RuntimeException("Could not find this Wrapped.")
        val userName = userService.getUsernameCached(userId)
            ?: throw RuntimeException("Could not find this Wrapped.")

        logger.info("Generating Wrapped for $userName in $year using $wrappedInfo")

        return WrappedView(
            name = userName,
            year = year,
            sections = listOfNotNull(
                WelcomeSection(year, userName),
                StatSection(
                    topText = "You played...",
                    stat = wrappedInfo.totalGamesPlayed,
                    bottomText = "...games this year.",
                ),
                StatSection(
                    topText = "That ranks...",
                    stat = wrappedInfo.totalGamesRank,
                    bottomText = "...across all players!",
                ),
                // points scored
                StatSection(
                    topText = "You scored...",
                    stat = wrappedInfo.totalPoints,
                    bottomText = "...points this year.",
                ),
                StatSection(
                    topText = "That ranks...",
                    stat = wrappedInfo.totalPointsRank,
                    bottomText = "...overall!",
                ),
                wrappedInfo.favoriteGame.let {
                    val favoriteGameText = "${it.emoji()}${it.displayName()}${it.emoji()}"
                    val favoriteGamePlayCount = wrappedInfo.gamesPlayedByGame[it]
                    TextSection(
                        topText = "Your favorite game was...",
                        middleText = favoriteGameText,
                        bottomText = "...you played it $favoriteGamePlayCount times!",
                    )
                },
                wrappedInfo.bestDay?.let {
                    TextSection(
                        topText = "Your best day was...",
                        middleText = it.format(bestDayFormatter),
                        bottomText = "...when you scored ${wrappedInfo.bestDayPoints} points!",
                    )
                },
                StatSection(
                    topText = "You played Daily Games for...",
                    stat = wrappedInfo.totalMinutes,
                    bottomText = "...minutes this year.",
                ),
                StatSection(
                    topText = "That's number...",
                    stat = wrappedInfo.totalMinutesRank,
                    bottomText = "... of all players!",
                ),
                wrappedInfo.bestGame?.let {
                    val bestGameText = "${it.emoji()}${it.displayName()}${it.emoji()}"
                    TextSection(
                        topText = "Your best game was...",
                        middleText = bestGameText,
                        bottomText = "",
                    )
                },
                wrappedInfo.bestGame?.let {
                    val bestGameAverage = wrappedInfo.averagesByGame[it]!!
                    val bestGameRank = wrappedInfo.ranksPerGameAverage[it]!!
                    TextSection(
                        topText = "Your average ${it.displayName()} score was...",
                        middleText = bestGameAverage.toString(),
                        bottomText = "...which ranks #$bestGameRank!",
                        fontSizeOverride = "35vw;"
                    )
                },
                SummaryTableSection(
                    wrappedInfo = wrappedInfo
                ),
                wrappedInfo.ranksPerGameTotal.let { ranks ->
                    val rows = ranks.entries.map { (game, rank) ->
                        RanksTableRowView(
                            game = game,
                            statText = (wrappedInfo.pointsByGame[game] ?: 0).toString(),
                            rank = rank,
                        )
                    }
                    RanksTableSection(
                        title = "Totals",
                        heading = "Points",
                        subHeading = null,
                        rows = rows
                    )
                },
                wrappedInfo.ranksPerGameAverage.let { ranks ->
                    val rows = ranks.entries.map { (game, rank) ->
                        RanksTableRowView(
                            game = game,
                            statText = (wrappedInfo.averagesByGame[game] ?: 0.0).toString(),
                            rank = rank,
                        )
                    }
                    RanksTableSection(
                        title = "Averages",
                        heading = "Average",
                        subHeading = "(min $MINIMUM_GAMES_FOR_AVERAGE games)",
                        rows = rows
                    )
                },
            )
        )
    }

    fun generateWrappedData(year: Int): List<WrappedInfo> = jdbi.open().use { handle ->
        val resultDAO = handle.attach<PuzzleResultDAO>()
        val yearStartInstant = LocalDate.ofYearDay(year, 1)
            .atStartOfDay(ZoneId.systemDefault())
            .toInstant()
        val yearEndInstant = LocalDate.ofYearDay(year + 1, 1)
            .atStartOfDay(ZoneId.systemDefault())
            .toInstant()
        val allResults = resultDAO.allResultsBetweenStream(
            start = yearStartInstant,
            end = yearEndInstant
        )

        val userIds = mutableSetOf<Long>()
        val totalGamesPlayed = mutableMapOf<Long, Int>()
        val gamesPlayedByGame = mutableMapOf<Long, MutableMap<Game, Int>>()
        val pointsByGame = mutableMapOf<Long, MutableMap<Game, Int>>()
        val previousGameInstant = mutableMapOf<Long, Instant>()
        val totalTimePlayed = mutableMapOf<Long, Duration>()
        val pointsByDay = mutableMapOf<Long, MutableMap<LocalDate, Int>>()
        // local cache of time zone
        val userTimeZones = mutableMapOf<Long, ZoneId>()

        // Iterate over the result stream and accumulate the data needed to create the WrappedInfo objects
        allResults.peek {
            userIds.add(it.userId)
        }.peek {
            gamesPlayedByGame.getOrPut(it.userId) { mutableMapOf() }
                .merge(it.game, 1, Int::plus)
        }.peek {
            pointsByGame.getOrPut(it.userId) { mutableMapOf() }
                .merge(it.game, calculator.calculatePoints(it), Int::plus)
        }.peek {
            previousGameInstant.put(it.userId, it.instantSubmitted)?.let { previous ->
                val difference = Duration.between(previous, it.instantSubmitted)
                if (difference < Duration.of(20, ChronoUnit.MINUTES)) {
                    totalTimePlayed.merge(it.userId, difference, Duration::plus)
                }
            }
        }.peek {
            val timeZone = userTimeZones.getOrPut(it.userId) {
                userPreferencesService.getTimeZone(it.userId)
            }
            pointsByDay.getOrPut(it.userId) { mutableMapOf() }
                .merge(
                    it.instantSubmitted.atZone(timeZone).toLocalDate(),
                    calculator.calculatePoints(it),
                    Int::plus
                )
        }.forEach {
            totalGamesPlayed.merge(it.userId, 1, Int::plus)
        }

        val usersRankedByGames = userIds.sortedByDescending { totalGamesPlayed[it] }
        val usersRankedByPoints = userIds.sortedByDescending { pointsByGame[it]?.values?.sum() ?: 0 }
        val usersRankedByTotalMinutes = userIds.sortedByDescending { totalTimePlayed[it]?.toMinutes() ?: 0 }
        val favoriteGameByUser = gamesPlayedByGame.mapValues { (_, gameCounts) ->
            // Take each user's most-played game, or a random one if they haven't played any
            gameCounts.maxByOrNull { (_, count) -> count }?.key ?: Game.values().first()
        }

        val averagesByUser = userIds.associateWith { userId ->
            Game.values().associateWith { game ->
                val gamesPlayed = gamesPlayedByGame[userId]?.get(game) ?: 0
                val points = pointsByGame[userId]?.get(game) ?: 0
                if (gamesPlayed == 0) {
                    0.0
                } else {
                    // round to 1 decimal place
                    Math.round(10.0 * points / gamesPlayed) / 10.0
                }
            }
        }

        val usersRankedByTotal = Game.values().associateWith { game ->
            userIds.sortedByDescending { pointsByGame[it]?.get(game) ?: 0 }
        }
        val usersRankedByAverage = Game.values().associateWith { game ->
            userIds
                .filter { userId -> (gamesPlayedByGame[userId]?.get(game) ?: 0) >= MINIMUM_GAMES_FOR_AVERAGE }
                .sortedByDescending { userId -> averagesByUser[userId]?.get(game) ?: 0.0 }
        }

        return userIds.map { userId ->
            val gamesPlayedByUser = gamesPlayedByGame[userId]?.keys ?: emptySet()
            val userRanksByGameTotal = gamesPlayedByUser
                .associateWith { game -> usersRankedByTotal[game]!!.indexOf(userId) + 1 }
            val userRanksByGameAverage = gamesPlayedByUser
                .filter { game -> usersRankedByAverage[game]?.contains(userId) ?: false }
                .associateWith { game -> usersRankedByAverage[game]!!.indexOf(userId) + 1 }
            val bestGame = userRanksByGameAverage
                .minByOrNull { (_, rank) -> rank }
                ?.key
            val bestDay = pointsByDay[userId]
                ?.maxByOrNull { (_, points) -> points }
                ?.key
            val bestDayPoints = pointsByDay[userId]?.get(bestDay) ?: 0

            WrappedInfo(
                id = 0,
                userId = userId,
                totalGamesPlayed = totalGamesPlayed[userId] ?: 0,
                totalGamesRank = usersRankedByGames.indexOf(userId) + 1,
                totalPoints = pointsByGame[userId]?.values?.sum() ?: 0,
                totalPointsRank = usersRankedByPoints.indexOf(userId) + 1,
                favoriteGame = favoriteGameByUser.getValue(userId),
                gamesPlayedByGame = gamesPlayedByGame[userId] ?: emptyMap(),
                bestDay = bestDay,
                bestDayPoints = bestDayPoints,
                totalMinutes = totalTimePlayed[userId]?.toMinutes()?.toInt() ?: 0,
                totalMinutesRank = usersRankedByTotalMinutes.indexOf(userId) + 1,
                bestGame = bestGame,
                pointsByGame = pointsByGame[userId] ?: emptyMap(),
                ranksPerGameTotal = userRanksByGameTotal,
                averagesByGame = averagesByUser[userId] ?: emptyMap(),
                ranksPerGameAverage = userRanksByGameAverage,
            )
        }
    }
}
