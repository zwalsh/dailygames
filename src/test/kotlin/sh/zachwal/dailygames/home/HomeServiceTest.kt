package sh.zachwal.dailygames.home

import com.google.common.truth.Truth.assertThat
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Test
import sh.zachwal.dailygames.db.dao.game.GameDAO
import sh.zachwal.dailygames.db.jdbi.User
import sh.zachwal.dailygames.db.jdbi.puzzle.Game
import sh.zachwal.dailygames.db.jdbi.puzzle.PuzzleResult
import sh.zachwal.dailygames.leaderboard.PointCalculator
import sh.zachwal.dailygames.nav.NavViewFactory
import sh.zachwal.dailygames.results.ResultService
import sh.zachwal.dailygames.results.resultinfo.TravleInfo
import sh.zachwal.dailygames.results.resultinfo.WorldleInfo
import sh.zachwal.dailygames.users.UserPreferencesService
import java.time.Clock
import java.time.Instant
import java.time.ZoneId

class HomeServiceTest {

    private val resultService = mockk<ResultService> {
        every { resultFeed(any()) } returns emptyList()
        every { resultsForUserToday(any()) } returns emptyList()
    }
    private val userPreferencesService = mockk<UserPreferencesService> {
        every { getTimeZone(any()) } returns ZoneId.of("America/New_York")
    }
    private val shareLineMapper = ShareLineMapper(
        pointCalculator = PointCalculator()
    )
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
        shareLineMapper = shareLineMapper,
        pointsCalculator = PointCalculator(),
        gameDAO = gameDAO,
        navViewFactory = navViewFactory,
        clock = clock,
    )

    private val worldleResult = PuzzleResult(
        id = 1L,
        userId = 1L,
        game = Game.WORLDLE,
        score = 5,
        puzzleNumber = 943,
        puzzleDate = null,
        instantSubmitted = Instant.now(),
        shareText = "",
        resultInfo = WorldleInfo(
            percentage = 100,
        ),
    )
    private val travleResult = PuzzleResult(
        id = 1L,
        userId = 1L,
        game = Game.TRAVLE,
        score = 5,
        puzzleNumber = 944,
        puzzleDate = null,
        instantSubmitted = Instant.now(),
        shareText = "",
        resultInfo = TravleInfo(
            numGuesses = 10,
            numIncorrect = 2,
            numPerfect = 3,
            numHints = 1,
        )
    )

    @Test
    fun `includes ShareTextModalView with a line per result plus points line`() {
        val user = User(id = 1L, username = "zach", hashedPassword = "123abc==")

        every { resultService.resultsForUserToday(user) } returns listOf(worldleResult, travleResult)

        val view = homeService.homeView(user)

        assertThat(view.shareTextModalView!!.shareTextLines).hasSize(3)
        assertThat(view.shareTextModalView!!.shareTextLines.take(2)).containsExactly(
            shareLineMapper.mapToShareLine(worldleResult),
            shareLineMapper.mapToShareLine(travleResult),
        )
    }

    @Test
    fun `ShareTextModal includes line for points`() {
        val user = User(id = 1L, username = "zach", hashedPassword = "123abc==")

        every { resultService.resultsForUserToday(user) } returns listOf(worldleResult, travleResult)

        val view = homeService.homeView(user)

        assertThat(view.shareTextModalView!!.shareTextLines).contains("Points: 3/12")
    }

    @Test
    fun `when no results, does not include modal`() {
        val user = User(id = 1L, username = "zach", hashedPassword = "123abc==")

        every { resultService.resultsForUserToday(user) } returns emptyList()

        val view = homeService.homeView(user)

        assertThat(view.shareTextModalView).isNull()
    }

    @Test
    fun `returns GameListView with all Games`() {
        val view = homeService.homeView(User(id = 1L, username = "zach", hashedPassword = "123abc=="))

        assertThat(view.gameListView.games).hasSize(Game.values().size)
        assertThat(view.gameListView.games.map { it.game }).containsExactly(*Game.values())
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
}
