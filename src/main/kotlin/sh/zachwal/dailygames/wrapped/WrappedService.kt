package sh.zachwal.dailygames.wrapped

import org.jdbi.v3.core.Jdbi
import org.jdbi.v3.sqlobject.kotlin.attach
import sh.zachwal.dailygames.db.dao.game.PuzzleResultDAO
import sh.zachwal.dailygames.db.jdbi.WrappedInfo
import sh.zachwal.dailygames.db.jdbi.puzzle.Game
import sh.zachwal.dailygames.leaderboard.PointCalculator
import sh.zachwal.dailygames.wrapped.views.RanksTableSection
import sh.zachwal.dailygames.wrapped.views.StatSection
import sh.zachwal.dailygames.wrapped.views.SummaryTableSection
import sh.zachwal.dailygames.wrapped.views.TextSection
import sh.zachwal.dailygames.wrapped.views.WelcomeSection
import sh.zachwal.dailygames.wrapped.views.WrappedView
import java.time.LocalDate
import java.time.ZoneId
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WrappedService @Inject constructor(
    private val jdbi: Jdbi,
    private val calculator: PointCalculator,
) {

    fun wrappedView(year: Int, wrappedId: String): WrappedView {
        return WrappedView(
            name = "zach",
            year = year,
            sections = listOf(
                WelcomeSection(year, "zach"),
                StatSection(
                    topText = "You played...",
                    stat = 123,
                    bottomText = "...games this year.",
                ),
                StatSection(
                    topText = "That ranks...",
                    stat = 15,
                    bottomText = "...across all players!",
                ),
                // points scored
                StatSection(
                    topText = "You scored...",
                    stat = 1234,
                    bottomText = "...points this year.",
                ),
                StatSection(
                    topText = "That ranks...",
                    stat = 10,
                    bottomText = "...overall!",
                ),
                // Favorite game
                TextSection(
                    topText = "Your favorite game was...",
                    middleText = "${Game.WORLDLE.emoji()}Worldle${Game.WORLDLE.emoji()}",
                    bottomText = "...you played it 123 times!",
                ),
                TextSection(
                    topText = "Your best day was...",
                    middleText = "September 14th",
                    bottomText = "...when you scored 39 points!",
                ),
                StatSection(
                    topText = "You played Daily Games for...",
                    stat = 1212,
                    bottomText = "...minutes this year.",
                ),
                StatSection(
                    topText = "That's number...",
                    stat = 3,
                    bottomText = "... of all players!",
                ),
                TextSection(
                    topText = "Your best game was...",
                    middleText = "${Game.WORLDLE.emoji()}Worldle${Game.WORLDLE.emoji()}",
                    bottomText = "",
                ),
                TextSection(
                    topText = "Your average Worldle score was...",
                    middleText = "5.5",
                    bottomText = "...which ranks #2!",
                    fontSizeOverride = "35vw;"
                ),
                SummaryTableSection(
                    f = "f"
                ),
                RanksTableSection(
                    title = "Totals",
                ),
                RanksTableSection(
                    title = "Averages",
                ),
            )
        )
    }

    fun generateWrappedData(year: Int): List<WrappedInfo> {
        val resultDAO = jdbi.open().attach<PuzzleResultDAO>()
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

        // Iterate over the result stream and accumulate the data needed to create the WrappedInfo objects
        allResults.peek {
            userIds.add(it.userId)
        }.peek {
            gamesPlayedByGame.getOrPut(it.userId) { mutableMapOf() }
                .merge(it.game, 1, Int::plus)
        }.peek {
            pointsByGame.getOrPut(it.userId) { mutableMapOf() }
                .merge(it.game, calculator.calculatePoints(it), Int::plus)
        }.forEach {
            totalGamesPlayed.merge(it.userId, 1, Int::plus)
        }

        val usersRankedByGames = userIds.sortedByDescending { totalGamesPlayed[it] }
        val usersRankedByPoints = userIds.sortedByDescending { pointsByGame[it]?.values?.sum() ?: 0 }

        return userIds.map {
            WrappedInfo(
                id = 0,
                userId = it,
                totalGamesPlayed = totalGamesPlayed[it] ?: 0,
                totalGamesRank = usersRankedByGames.indexOf(it) + 1,
                totalPoints = pointsByGame[it]?.values?.sum() ?: 0,
                totalPointsRank = usersRankedByPoints.indexOf(it) + 1,
                gamesPlayedByGame = gamesPlayedByGame[it] ?: emptyMap(),
                pointsByGame = pointsByGame[it] ?: emptyMap()
            )
        }
    }
}
