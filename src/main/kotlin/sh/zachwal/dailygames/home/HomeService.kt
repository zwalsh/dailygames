package sh.zachwal.dailygames.home

import sh.zachwal.dailygames.db.dao.game.GameDAO
import sh.zachwal.dailygames.db.jdbi.User
import sh.zachwal.dailygames.db.jdbi.puzzle.Game
import sh.zachwal.dailygames.home.views.HomeView
import sh.zachwal.dailygames.home.views.ShareTextModalView
import sh.zachwal.dailygames.home.views.gamelinks.GameLinkView
import sh.zachwal.dailygames.home.views.gamelinks.GameListView
import sh.zachwal.dailygames.leaderboard.PuzzleResultPointCalculator
import sh.zachwal.dailygames.nav.NavItem
import sh.zachwal.dailygames.nav.NavViewFactory
import sh.zachwal.dailygames.results.ResultService
import java.time.Instant
import java.time.temporal.ChronoUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class HomeService @Inject constructor(
    private val resultService: ResultService,
    private val shareLineMapper: ShareLineMapper,
    private val pointsCalculator: PuzzleResultPointCalculator,
    private val navViewFactory: NavViewFactory,
    private val gameDAO: GameDAO,
) {

    fun homeView(user: User): HomeView {

        val navView = navViewFactory.navView(
            username = user.username,
            currentActiveNavItem = NavItem.HOME,
        )

        return HomeView(
            resultFeed = resultService.resultFeed(user.id),
            shareTextModalView = shareTextModalView(user),
            gameListView = gameListView(),
            nav = navView,
        )
    }

    private fun gameListView(): GameListView {
        val newGames = gameDAO.listGamesCreatedAfter(Instant.now().minus(3, ChronoUnit.DAYS))
        // List the new games first
        val games = newGames + Game.values().filter { it !in newGames }
        val gameLinkViews = games.map { game ->
            GameLinkView(
                game = game,
                isNew = game in newGames,
            )
        }
        return GameListView(gameLinkViews)
    }

    private fun shareTextModalView(user: User): ShareTextModalView? {
        val results = resultService.resultsForUserToday(user)
        if (results.isEmpty()) {
            return null
        }

        val shareTextLines = results
            .map(shareLineMapper::mapToShareLine)

        val points = results.sumOf { pointsCalculator.calculatePoints(it) }
        val maxPoints = results.sumOf { pointsCalculator.maxPoints(it) }

        val pointsLine = "Points: $points/$maxPoints"

        return ShareTextModalView(shareTextLines + pointsLine)
    }
}
