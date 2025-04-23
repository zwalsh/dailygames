package sh.zachwal.dailygames.home

import com.google.common.truth.Truth.assertThat
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Test
import sh.zachwal.dailygames.db.dao.game.GameDAO
import sh.zachwal.dailygames.db.jdbi.User
import sh.zachwal.dailygames.db.jdbi.puzzle.Game
import sh.zachwal.dailygames.home.views.ShareTextModalView
import sh.zachwal.dailygames.leaderboard.LeaderboardService
import sh.zachwal.dailygames.nav.NavViewFactory
import sh.zachwal.dailygames.results.ResultService
import sh.zachwal.dailygames.users.UserPreferencesService
import sh.zachwal.dailygames.users.UserService
import java.time.Clock
import java.time.Instant
import java.time.ZoneId

class HomeServiceTest {

    private val resultService = mockk<ResultService> {
        every { resultFeed(any()) } returns emptyList()
        every { resultsForUserToday(any()) } returns emptyList()
        every { resultCountByGame(any(), any()) } returns emptyMap()
    }
    private val userPreferencesService = mockk<UserPreferencesService> {
        every { getTimeZone(any()) } returns ZoneId.of("America/New_York")
    }
    private val userService = mockk<UserService> {
        every { getUsernameCached(1L) } returns "zach"
        every { getUsernameCached(2L) } returns "zach2"
        every { getUsernameCached(3L) } returns "zach3"
        every { getUsernameCached(4L) } returns "zach4"
        every { getUsernameCached(5L) } returns "zach5"
    }
    private val leaderboardService = mockk<LeaderboardService> {
        every { dailyLeaderboard(any()) } returns mapOf(
            1L to 40,
            2L to 39,
            3L to 38,
            4L to 37,
            5L to 36,
        )
    }
    private val shareTextService = mockk<ShareTextService>() {
        every { shareTextModalView(any()) } returns null
    }
    private val navViewFactory = mockk<NavViewFactory> {
        every { navView(any(), any()) } returns mockk()
    }

    private val gameDAO = mockk<GameDAO> {
        every { listGamesCreatedAfter(any()) } returns emptyList()
    }
    private val clock = mockk<Clock> {
        every { instant() } returns Instant.now()
    }

    private val homeService = HomeService(
        resultService = resultService,
        userPreferencesService = userPreferencesService,
        userService = userService,
        leaderboardService = leaderboardService,
        shareTextService = shareTextService,
        gameDAO = gameDAO,
        navViewFactory = navViewFactory,
        clock = clock,
    )

    @Test
    fun `includes ShareTextModalView `() {
        val user = User(id = 1L, username = "zach", hashedPassword = "123abc==")

        every { shareTextService.shareTextModalView(user) } returns ShareTextModalView(emptyList())

        val view = homeService.homeView(user)

        assertThat(view.shareTextModalView).isNotNull()
    }

    @Test
    fun `when no results, does not include modal`() {
        val user = User(id = 1L, username = "zach", hashedPassword = "123abc==")

        every { resultService.resultsForUserToday(user) } returns emptyList()

        val view = homeService.homeView(user)

        assertThat(view.shareTextModalView).isNull()
    }

    @Test
    fun `returns GameListView with all Games except hidden ones`() {
        val view = homeService.homeView(User(id = 1L, username = "zach", hashedPassword = "123abc=="))

        val expectedSize = Game.values().size - hiddenGames.size
        assertThat(view.gameListView.games).hasSize(expectedSize)
        val expectedElements = Game.values().toSet() - hiddenGames
        assertThat(view.gameListView.games.map { it.game }).containsExactlyElementsIn(expectedElements)
    }

    @Test
    fun `GameListView has new games first`() {
        val newGames = listOf(Game.WORLDLE, Game.TRAVLE)
        every { gameDAO.listGamesCreatedAfter(any()) } returns newGames

        val view = homeService.homeView(User(id = 1L, username = "zach", hashedPassword = "123abc=="))

        val games = view.gameListView.games.map { it.game }

        assertThat(games.take(2)).containsExactly(Game.WORLDLE, Game.TRAVLE).inOrder()
        assertThat(games).containsNoDuplicates()
    }

    @Test
    fun `GameListView marks new games as new`() {
        val newGames = listOf(Game.WORLDLE, Game.TRAVLE)
        every { gameDAO.listGamesCreatedAfter(any()) } returns newGames

        val view = homeService.homeView(User(id = 1L, username = "zach", hashedPassword = "123abc=="))

        val gameLinkViews = view.gameListView.games

        assertThat(gameLinkViews.find { it.game == Game.WORLDLE }!!.isNew).isTrue()
        assertThat(gameLinkViews.find { it.game == Game.TRAVLE }!!.isNew).isTrue()
    }

    @Test
    fun `in middle of year, homeView() does not include wrapped link`() {
        every { clock.instant() } returns Instant.parse("2022-06-01T00:00:00Z")

        val view = homeService.homeView(User(id = 1L, username = "zach", hashedPassword = "123abc=="))

        assertThat(view.wrappedLinkView).isNull()
    }

    @Test
    fun `in first 7 days of year, homeView() includes wrapped link for last year`() {
        every { clock.instant() } returns Instant.parse("2022-01-07T00:00:00Z")

        val view = homeService.homeView(User(id = 1L, username = "zach", hashedPassword = "123abc=="))

        assertThat(view.wrappedLinkView).isNotNull()
        assertThat(view.wrappedLinkView!!.year).isEqualTo(2021)
    }

    @Test
    fun `first 7 days of year is relative to user's timezone`() {
        // 1/8/25 at 1am ET is still 1/7 in PT
        every { clock.instant() } returns Instant.ofEpochSecond(1736316000)
        every { userPreferencesService.getTimeZone(any()) } returns ZoneId.of("America/Los_Angeles")

        val view = homeService.homeView(User(id = 1L, username = "zach", hashedPassword = "123abc=="))

        assertThat(view.wrappedLinkView).isNotNull()
        assertThat(view.wrappedLinkView!!.year).isEqualTo(2024)
    }

    @Test
    fun `sorts games by play count`() {
        every { resultService.resultCountByGame(any(), any()) } returns mapOf(
            Game.WORLDLE to 5,
            Game.TRAVLE to 3,
            Game.TOP5 to 1,
        )
        val otherGames = Game.values().toSet() - setOf(Game.WORLDLE, Game.TRAVLE, Game.TOP5) - hiddenGames

        val view = homeService.homeView(User(id = 1L, username = "zach", hashedPassword = "123abc=="))
        val games = view.gameListView.games.map { it.game }

        assertThat(games)
            .containsExactly(
                Game.WORLDLE,
                Game.TRAVLE,
                Game.TOP5,
                *otherGames.toTypedArray(),
            ).inOrder()
    }

    @Test
    fun `caches result count`() {
        every { resultService.resultCountByGame(any(), any()) } returns
            mapOf(Game.WORLDLE to 10) andThen
            mapOf(Game.TRAVLE to 10)

        homeService.homeView(User(id = 1L, username = "zach", hashedPassword = "123abc=="))
        val view = homeService.homeView(User(id = 1L, username = "zach", hashedPassword = "123abc=="))

        assertThat(view.gameListView.games.first().game).isEqualTo(Game.WORLDLE)
    }

    @Test
    fun `includes daily leaderboard`() {
        val view = homeService.homeView(User(id = 1L, username = "zach", hashedPassword = "123abc=="))

        assertThat(view.dailyLeaderboardView).isNotNull()
        assertThat(view.dailyLeaderboardView.dailyPerformances).isEqualTo(
            listOf(
                "zach" to 40,
                "zach2" to 39,
                "zach3" to 38,
                "zach4" to 37,
                "zach5" to 36,
            )
        )
    }

    @Test
    fun `does not include daily leaderboard if there are no scorers yet`() {
        every { leaderboardService.dailyLeaderboard(any()) } returns emptyMap()

        val view = homeService.homeView(User(id = 1L, username = "zach", hashedPassword = "123abc=="))

        assertThat(view.dailyLeaderboardView).isNull()
    }
}
