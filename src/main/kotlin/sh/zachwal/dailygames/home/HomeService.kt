package sh.zachwal.dailygames.home

import com.google.common.cache.CacheBuilder
import com.google.common.cache.CacheLoader
import com.google.common.cache.LoadingCache
import sh.zachwal.dailygames.db.dao.game.GameDAO
import sh.zachwal.dailygames.db.jdbi.User
import sh.zachwal.dailygames.db.jdbi.puzzle.Game
import sh.zachwal.dailygames.home.views.DailyLeaderboardView
import sh.zachwal.dailygames.home.views.HomeView
import sh.zachwal.dailygames.home.views.WrappedLinkView
import sh.zachwal.dailygames.home.views.gamelinks.GameLinkView
import sh.zachwal.dailygames.home.views.gamelinks.GameListView
import sh.zachwal.dailygames.nav.NavItem
import sh.zachwal.dailygames.nav.NavViewFactory
import sh.zachwal.dailygames.results.ResultService
import sh.zachwal.dailygames.users.UserPreferencesService
import java.time.Clock
import java.time.Duration
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

// Hide these games from the list
val hiddenGames = setOf(
    // Pinpoint is not currently working & no one plays it anyway
    Game.PINPOINT
)

@Singleton
class HomeService @Inject constructor(
    private val resultService: ResultService,
    private val userPreferencesService: UserPreferencesService,
    private val shareTextService: ShareTextService,
    private val navViewFactory: NavViewFactory,
    private val gameDAO: GameDAO,
    private val clock: Clock,
) {
    private val newGameDuration = Duration.ofDays(3)

    private val gameCountKey = "key"
    private val resultCountCache: LoadingCache<Any, Map<Game, Int>> = CacheBuilder.newBuilder()
        .expireAfterWrite(1, TimeUnit.DAYS)
        .build(
            object : CacheLoader<Any, Map<Game, Int>>() {
                override fun load(key: Any): Map<Game, Int> {
                    return resultService.resultCountByGame(
                        since = Instant.now().minus(30, ChronoUnit.DAYS),
                        excludeUserId = 1, // Exclude my user in prod since I play the most
                    )
                }
            }
        )

    fun homeView(user: User): HomeView {
        val navView = navViewFactory.navView(
            username = user.username,
            currentActiveNavItem = NavItem.HOME,
        )

        val userTimeZone = userPreferencesService.getTimeZone(user.id)
        val localDate = clock.instant().atZone(userTimeZone).toLocalDate()

        val wrappedLinkView = if (localDate.dayOfYear <= 7) {
            // Wrapped is for last year
            WrappedLinkView(localDate.year - 1)
        } else {
            null
        }

        return HomeView(
            resultFeed = resultService.resultFeed(user.id),
            shareTextModalView = shareTextService.shareTextModalView(user),
            wrappedLinkView = wrappedLinkView,
            dailyLeaderboardView = dailyLeaderboardView(user),
            gameListView = gameListView(),
            nav = navView,
        )
    }

    private fun gameListView(): GameListView {
        // List the new games first
        val newGames = gameDAO.listGamesCreatedAfter(Instant.now().minus(newGameDuration))
        val otherGames = Game.values()
            .filter { it !in newGames }

        val resultCountByGame = resultCountCache[gameCountKey]
        val games = newGames + otherGames.sortedByDescending { resultCountByGame[it] ?: 0 }

        val gameLinkViews = games
            .filter { it !in hiddenGames }
            .map { game ->
                GameLinkView(
                    game = game,
                    isNew = game in newGames,
                )
            }
        return GameListView(gameLinkViews)
    }

    private fun dailyLeaderboardView(user: User): DailyLeaderboardView? {
        return if (resultService.anyResultsToday(user)) {
            DailyLeaderboardView
        } else {
            null
        }
    }
}
