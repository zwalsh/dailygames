package sh.zachwal.dailygames.leaderboard

import com.google.common.truth.Truth.assertThat
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.jdbi.v3.core.Jdbi
import org.jdbi.v3.sqlobject.kotlin.attach
import sh.zachwal.dailygames.db.dao.game.PuzzleResultDAO
import sh.zachwal.dailygames.db.jdbi.User
import sh.zachwal.dailygames.db.jdbi.puzzle.Game
import sh.zachwal.dailygames.db.jdbi.puzzle.PuzzleResult
import sh.zachwal.dailygames.nav.NavItem
import sh.zachwal.dailygames.nav.NavViewFactory
import sh.zachwal.dailygames.results.resultinfo.Top5Info
import sh.zachwal.dailygames.results.resultinfo.WorldleInfo
import sh.zachwal.dailygames.users.UserPreferencesService
import sh.zachwal.dailygames.users.UserService
import java.time.Instant
import java.time.ZoneId
import java.time.temporal.ChronoUnit
import java.util.stream.Stream
import kotlin.test.Test

class LeaderboardServiceTest {
    private val resultDAO: PuzzleResultDAO = mockk {
        every { allResultsForGameStream(any()) } answers {
            Stream.empty()
        }
    }

    private val testUser = User(id = 1L, username = "test", hashedPassword = "test")
    private val derekUser = User(id = 2L, username = "derknasty", hashedPassword = "test")
    private val jackieUser = User(id = 3L, username = "jackiewalsh", hashedPassword = "test")
    private val chatGPTUser = User(id = 4L, username = "ChatGPT", hashedPassword = "test")
    private val mikMapUser = User(id = 5L, username = "MikMap", hashedPassword = "test")
    private val zachUser = User(id = 6L, username = "zach", hashedPassword = "test")

    private val userService: UserService = mockk {
        every { getUser(1) } returns testUser
        every { getUser(2) } returns derekUser
        every { getUser(3) } returns jackieUser
        every { getUser(4) } returns chatGPTUser
        every { getUser(5) } returns mikMapUser
        every { getUser(6) } returns zachUser
    }
    private val userPreferencesService: UserPreferencesService = mockk {
        every { getTimeZoneCached(any()) } returns ZoneId.of("America/New_York")
    }
    private val navViewFactory = mockk<NavViewFactory>(relaxed = true)
    private val jdbi = mockk<Jdbi> {
        every { open() } returns mockk(relaxed = true) {
            every { attach<PuzzleResultDAO>() } returns resultDAO
        }
    }
    private val leaderboardService = LeaderboardService(
        userService = userService,
        userPreferencesService = userPreferencesService,
        jdbi = jdbi,
        pointCalculator = PointCalculator(),
        navViewFactory = navViewFactory,
        minimumGamesForAverage = 1,
    )

    private val result = PuzzleResult(
        id = 1L,
        userId = 1L,
        game = Game.TOP5,
        score = 5,
        puzzleNumber = 30,
        puzzleDate = null,
        instantSubmitted = Instant.now(),
        shareText = "",
        resultInfo = Top5Info(
            numGuesses = 5,
            numCorrect = 0,
            isPerfect = false,
        )
    )

    @Test
    fun `leaderboardView creates NavView with LEADERBOARD as active item`() {
        leaderboardService.overallLeaderboardView(testUser)

        verify {
            navViewFactory.navView(
                username = testUser.username,
                currentActiveNavItem = NavItem.LEADERBOARD
            )
        }
    }

    @Test
    fun `gameLeaderboardView returns GameLeaderboardView with game set`() {
        val gameLeaderboardView = leaderboardService.gameLeaderboardView(testUser, Game.TOP5)

        assertThat(gameLeaderboardView.game).isEqualTo(Game.TOP5)
    }

    @Test
    fun `gameLeaderboardData returns user's average all time`() {
        every { resultDAO.allResultsForGameStream(Game.TOP5) } returns Stream.of(
            result.copy(score = 5),
            result.copy(score = 4)
        )

        val leaderboardData = leaderboardService.gameLeaderboardData(Game.TOP5)

        assertThat(leaderboardData.allTimeAverage.labels).containsExactly(testUser.username)
        assertThat(leaderboardData.allTimeAverage.dataPoints).containsExactly(4.5)
    }

    @Test
    fun `gameLeaderboardData returns user's total points`() {
        every { resultDAO.allResultsForGameStream(Game.TOP5) } returns Stream.of(
            result.copy(score = 5),
            result.copy(score = 4)
        )

        val leaderboardData = leaderboardService.gameLeaderboardData(Game.TOP5)

        assertThat(leaderboardData.allTimePoints.labels).containsExactly(testUser.username)
        assertThat(leaderboardData.allTimePoints.dataPoints).containsExactly(9.0)
    }

    @Test
    fun `gameLeaderboardData returns user's total games played`() {
        every { resultDAO.allResultsForGameStream(Game.TOP5) } returns Stream.of(
            result.copy(score = 5),
            result.copy(score = 4)
        )

        val leaderboardData = leaderboardService.gameLeaderboardData(Game.TOP5)

        assertThat(leaderboardData.allTimeGames.labels).containsExactly(testUser.username)
        assertThat(leaderboardData.allTimeGames.dataPoints).containsExactly(2.0)
    }

    @Test
    fun `gameLeaderboardData filters to user's last thirty days`() {
        every { resultDAO.allResultsForGameStream(Game.TOP5) } returns Stream.of(
            result.copy(score = 5, instantSubmitted = Instant.now()),
            result.copy(score = 4, instantSubmitted = Instant.now().minus(31, ChronoUnit.DAYS))
        )

        val leaderboardData = leaderboardService.gameLeaderboardData(Game.TOP5)

        assertThat(leaderboardData.thirtyDaysAverage.labels).containsExactly(testUser.username)
        assertThat(leaderboardData.thirtyDaysAverage.dataPoints).containsExactly(5.0)
    }

    @Test
    fun `gameLeaderboardData returns total in last thirty days`() {
        every { resultDAO.allResultsForGameStream(Game.TOP5) } returns Stream.of(
            result.copy(score = 5, instantSubmitted = Instant.now()),
            result.copy(score = 6, instantSubmitted = Instant.now()),
            result.copy(score = 4, instantSubmitted = Instant.now().minus(31, ChronoUnit.DAYS))
        )

        val leaderboardData = leaderboardService.gameLeaderboardData(Game.TOP5)

        assertThat(leaderboardData.thirtyDaysPoints.labels).containsExactly(testUser.username)
        assertThat(leaderboardData.thirtyDaysPoints.dataPoints).containsExactly(11.0)
    }

    @Test
    fun `gameLeaderboardData returns total games in last thirty days`() {
        every { resultDAO.allResultsForGameStream(Game.TOP5) } returns Stream.of(
            result.copy(score = 5, instantSubmitted = Instant.now()),
            result.copy(score = 6, instantSubmitted = Instant.now()),
            result.copy(score = 4, instantSubmitted = Instant.now().minus(31, ChronoUnit.DAYS))
        )

        val leaderboardData = leaderboardService.gameLeaderboardData(Game.TOP5)

        assertThat(leaderboardData.thirtyDaysGames.labels).containsExactly(testUser.username)
        assertThat(leaderboardData.thirtyDaysGames.dataPoints).containsExactly(2.0)
    }

    @Test
    fun `gameLeaderboardData fetches from correct DAO`() {
        val worldleResult = PuzzleResult(
            id = 1L,
            userId = testUser.id,
            game = Game.WORLDLE,
            score = 4,
            puzzleNumber = 30,
            puzzleDate = null,
            instantSubmitted = Instant.now(),
            shareText = "",
            resultInfo = WorldleInfo(
                percentage = 100,
            )
        )
        every { resultDAO.allResultsForGameStream(Game.TOP5) } returns Stream.empty()
        every { resultDAO.allResultsForGameStream(Game.WORLDLE) } returns Stream.of(
            worldleResult
        )

        val leaderboardData = leaderboardService.gameLeaderboardData(Game.WORLDLE)
        val expectedPoints = PointCalculator().calculatePoints(worldleResult).toDouble()

        assertThat(leaderboardData.allTimeAverage.labels).containsExactly(testUser.username)
        assertThat(leaderboardData.allTimeAverage.dataPoints).containsExactly(expectedPoints)
    }

    @Test
    fun `gameLeaderboardData includes only the top 5 users all time`() {
        every { resultDAO.allResultsForGameStream(Game.TOP5) } returns Stream.of(
            result.copy(userId = zachUser.id, score = 5),

            result.copy(userId = testUser.id, score = 10),
            result.copy(userId = derekUser.id, score = 9),
            result.copy(userId = jackieUser.id, score = 8),
            result.copy(userId = chatGPTUser.id, score = 7),
            result.copy(userId = mikMapUser.id, score = 6)
        )

        val leaderboardData = leaderboardService.gameLeaderboardData(Game.TOP5)

        assertThat(leaderboardData.allTimeAverage.labels).containsExactly(
            testUser.username,
            derekUser.username,
            jackieUser.username,
            chatGPTUser.username,
            mikMapUser.username
        )
        assertThat(leaderboardData.allTimeAverage.labels).doesNotContain(zachUser.username)
    }

    private val worldleResult = PuzzleResult(
        id = 1L,
        userId = 1L,
        game = Game.WORLDLE,
        score = 4,
        puzzleNumber = 30,
        puzzleDate = null,
        instantSubmitted = Instant.now(),
        shareText = "",
        resultInfo = WorldleInfo(
            percentage = 100,
        )
    )

    @Test
    fun `overall leaderboardData averages user's points across all games`() {
        every { resultDAO.allResultsForGameStream(Game.TOP5) } returns Stream.of(
            result.copy(score = 5),
            result.copy(score = 4)
        )
        every { resultDAO.allResultsForGameStream(Game.WORLDLE) } returns Stream.of(
            worldleResult.copy(score = 2), // 5 points
            worldleResult.copy(score = 1) // 6 points
        )

        val leaderboardData = leaderboardService.overallLeaderboardData()

        assertThat(leaderboardData.allTimeAverage.labels).containsExactly(testUser.username)
        assertThat(leaderboardData.allTimeAverage.dataPoints).containsExactly(5.0)
    }

    @Test
    fun `overall leaderboardData sums user's points across all games`() {
        every { resultDAO.allResultsForGameStream(Game.TOP5) } returns Stream.of(
            result.copy(score = 5),
            result.copy(score = 4)
        )
        every { resultDAO.allResultsForGameStream(Game.WORLDLE) } returns Stream.of(
            worldleResult.copy(score = 2), // 5 points
            worldleResult.copy(score = 1) // 6 points
        )

        val leaderboardData = leaderboardService.overallLeaderboardData()

        assertThat(leaderboardData.allTimePoints.labels).containsExactly(testUser.username)
        assertThat(leaderboardData.allTimePoints.dataPoints).containsExactly(20.0)
    }

    @Test
    fun `overall leaderboardData sums user's games played`() {
        every { resultDAO.allResultsForGameStream(Game.TOP5) } returns Stream.of(
            result.copy(score = 5),
            result.copy(score = 4)
        )
        every { resultDAO.allResultsForGameStream(Game.WORLDLE) } returns Stream.of(
            worldleResult.copy(score = 2), // 5 points
            worldleResult.copy(score = 1) // 6 points
        )

        val leaderboardData = leaderboardService.overallLeaderboardData()

        assertThat(leaderboardData.allTimeGames.labels).containsExactly(testUser.username)
        assertThat(leaderboardData.allTimeGames.dataPoints).containsExactly(4.0)
    }

    @Test
    fun `overall leaderboardData includes multiple users`() {
        every { resultDAO.allResultsForGameStream(Game.TOP5) } returns Stream.of(
            result.copy(userId = derekUser.id, score = 5),
            result.copy(userId = jackieUser.id, score = 4)
        )
        every { resultDAO.allResultsForGameStream(Game.WORLDLE) } returns Stream.of(
            worldleResult.copy(userId = derekUser.id, score = 2), // 5 points
            worldleResult.copy(userId = jackieUser.id, score = 3) // 4 points
        )

        val leaderboardData = leaderboardService.overallLeaderboardData()

        assertThat(leaderboardData.allTimeAverage.labels).containsExactly(
            derekUser.username,
            jackieUser.username
        )
        assertThat(leaderboardData.allTimeAverage.dataPoints).containsExactly(5.0, 4.0)
    }

    @Test
    fun `overall leaderboardData filters by thirty days across games`() {
        every { resultDAO.allResultsForGameStream(Game.TOP5) } returns Stream.of(
            result.copy(score = 5, instantSubmitted = Instant.now()),
            result.copy(
                score = 4, instantSubmitted = Instant.now().minus(31, ChronoUnit.DAYS)
            )
        )
        every { resultDAO.allResultsForGameStream(Game.WORLDLE) } returns Stream.of(
            worldleResult.copy(
                score = 2, // 5 points
                instantSubmitted = Instant.now()
            ),
            worldleResult.copy(
                score = 3, // 4 points
                instantSubmitted = Instant.now().minus(31, ChronoUnit.DAYS)
            )
        )

        val leaderboardData = leaderboardService.overallLeaderboardData()

        assertThat(leaderboardData.thirtyDaysAverage.labels).containsExactly(testUser.username)
        assertThat(leaderboardData.thirtyDaysAverage.dataPoints).containsExactly(5.0)

        assertThat(leaderboardData.thirtyDaysPoints.labels).containsExactly(testUser.username)
        assertThat(leaderboardData.thirtyDaysPoints.dataPoints).containsExactly(10.0)

        assertThat(leaderboardData.thirtyDaysGames.labels).containsExactly(testUser.username)
        assertThat(leaderboardData.thirtyDaysGames.dataPoints).containsExactly(2.0)
    }

    @Test
    fun `average requires minimum ten games for per-game, all time and thirty days`() {
        val leaderboardService = LeaderboardService(
            userService = userService,
            userPreferencesService = userPreferencesService,
            jdbi = jdbi,
            pointCalculator = PointCalculator(),
            navViewFactory = navViewFactory,
            minimumGamesForAverage = 10,
        )

        val zachList = List(10) { result.copy(userId = zachUser.id, score = 5) }
        val jackieList = List(9) { result.copy(userId = jackieUser.id, score = 6) }
        every { resultDAO.allResultsForGameStream(Game.TOP5) } returns (zachList + jackieList).stream()

        val leaderboardData = leaderboardService.gameLeaderboardData(Game.TOP5)

        assertThat(leaderboardData.allTimeAverage.labels).containsExactly(zachUser.username)
        assertThat(leaderboardData.thirtyDaysAverage.labels).containsExactly(zachUser.username)
    }

    @Test
    fun `average requires minimum ten games for overall, all time and thirty days`() {
        val leaderboardService = LeaderboardService(
            userService = userService,
            userPreferencesService = userPreferencesService,
            jdbi = jdbi,
            pointCalculator = PointCalculator(),
            navViewFactory = navViewFactory,
            minimumGamesForAverage = 10,
        )

        val zachTop5List = List(5) { result.copy(userId = zachUser.id, score = 5) }
        val jackieTop5List = List(5) { result.copy(userId = jackieUser.id, score = 6) }
        every { resultDAO.allResultsForGameStream(Game.TOP5) } returns (zachTop5List + jackieTop5List).stream()
        val zachWorldle5List = List(5) { worldleResult.copy(userId = zachUser.id, score = 5) }
        val jackieWorldleList = List(4) { worldleResult.copy(userId = jackieUser.id, score = 6) }
        every { resultDAO.allResultsForGameStream(Game.WORLDLE) } returns (zachWorldle5List + jackieWorldleList).stream()

        val leaderboardData = leaderboardService.overallLeaderboardData()

        assertThat(leaderboardData.allTimeAverage.labels).containsExactly(zachUser.username)
        assertThat(leaderboardData.thirtyDaysAverage.labels).containsExactly(zachUser.username)
    }

    @Test
    fun `pointsHistogram includes percentage of games at each point value in correct order and format`() {
        every { resultDAO.allResultsForGameStream(Game.GEOCIRCLES) } returns Stream.of(
            result.copy(score = 1),
            result.copy(score = 1),
            result.copy(score = 1),
            result.copy(score = 2),
            result.copy(score = 2),
            result.copy(score = 3),
        )

        val leaderboardData = leaderboardService.gameLeaderboardData(Game.GEOCIRCLES)

        assertThat(leaderboardData.pointsHistogram.labels)
            .containsExactly("3", "2", "1")
            .inOrder()
        assertThat(leaderboardData.pointsHistogram.dataPoints)
            .containsExactly(16.7, 33.3, 50.0)
            .inOrder()
    }

    @Test
    fun `dailyLeaderboard returns single user with correct points`() {
        every { resultDAO.allResultsBetweenStream(any(), any()) } returns Stream.of(
            result.copy(userId = testUser.id, score = 5)
        )

        val leaderboard = leaderboardService.dailyLeaderboard(testUser.id)

        assertThat(leaderboard).containsEntry(testUser.id, 5)
    }

    @Test
    fun `dailyLeaderboard aggregates multiple results for one user`() {
        every { resultDAO.allResultsBetweenStream(any(), any()) } returns Stream.of(
            result.copy(userId = testUser.id, score = 5),
            result.copy(userId = testUser.id, score = 10)
        )

        val leaderboard = leaderboardService.dailyLeaderboard(testUser.id)

        assertThat(leaderboard).containsEntry(testUser.id, 15)
    }

    @Test
    fun `dailyLeaderboard returns top 5 users when more than 5 users exist`() {
        every { resultDAO.allResultsBetweenStream(any(), any()) } returns Stream.of(
            result.copy(userId = testUser.id, score = 10),
            result.copy(userId = derekUser.id, score = 9),
            result.copy(userId = jackieUser.id, score = 8),
            result.copy(userId = chatGPTUser.id, score = 7),
            result.copy(userId = mikMapUser.id, score = 6),
            result.copy(userId = zachUser.id, score = 5)
        )

        val leaderboard = leaderboardService.dailyLeaderboard(testUser.id)

        assertThat(leaderboard).containsExactly(
            testUser.id, 10,
            derekUser.id, 9,
            jackieUser.id, 8,
            chatGPTUser.id, 7,
            mikMapUser.id, 6
        )
        assertThat(leaderboard).doesNotContainKey(zachUser.id)
    }

    @Test
    fun `dailyLeaderboard handles tied scores correctly`() {
        every { resultDAO.allResultsBetweenStream(any(), any()) } returns Stream.of(
            result.copy(userId = testUser.id, score = 10),
            result.copy(userId = derekUser.id, score = 10),
            result.copy(userId = jackieUser.id, score = 8),
            result.copy(userId = chatGPTUser.id, score = 7),
            result.copy(userId = mikMapUser.id, score = 6),
            result.copy(userId = zachUser.id, score = 5)
        )

        val leaderboard = leaderboardService.dailyLeaderboard(testUser.id)

        assertThat(leaderboard).containsExactly(
            testUser.id, 10,
            derekUser.id, 10,
            jackieUser.id, 8,
            chatGPTUser.id, 7,
            mikMapUser.id, 6
        )
        assertThat(leaderboard).doesNotContainKey(zachUser.id)
    }

    @Test
    fun `dailyLeaderboardData returns single user with correct points`() {
        every { resultDAO.allResultsBetweenStream(any(), any()) } returns Stream.of(
            result.copy(userId = testUser.id, score = 5)
        )
        every { userService.getUsernameCached(testUser.id) } returns testUser.username

        val chartInfo = leaderboardService.dailyLeaderboardData(testUser.id)

        assertThat(chartInfo.labels).containsExactly(testUser.username)
        assertThat(chartInfo.dataPoints).containsExactly(5.0)
    }

    @Test
    fun `dailyLeaderboardData aggregates multiple results for one user`() {
        every { resultDAO.allResultsBetweenStream(any(), any()) } returns Stream.of(
            result.copy(userId = testUser.id, score = 5),
            result.copy(userId = testUser.id, score = 10)
        )
        every { userService.getUsernameCached(testUser.id) } returns testUser.username

        val chartInfo = leaderboardService.dailyLeaderboardData(testUser.id)

        assertThat(chartInfo.labels).containsExactly(testUser.username)
        assertThat(chartInfo.dataPoints).containsExactly(15.0)
    }

    @Test
    fun `dailyLeaderboardData returns top 5 users when more than 5 users exist`() {
        every { resultDAO.allResultsBetweenStream(any(), any()) } returns Stream.of(
            result.copy(userId = testUser.id, score = 10),
            result.copy(userId = derekUser.id, score = 9),
            result.copy(userId = jackieUser.id, score = 8),
            result.copy(userId = chatGPTUser.id, score = 7),
            result.copy(userId = mikMapUser.id, score = 6),
            result.copy(userId = zachUser.id, score = 5)
        )
        every { userService.getUsernameCached(testUser.id) } returns testUser.username
        every { userService.getUsernameCached(derekUser.id) } returns derekUser.username
        every { userService.getUsernameCached(jackieUser.id) } returns jackieUser.username
        every { userService.getUsernameCached(chatGPTUser.id) } returns chatGPTUser.username
        every { userService.getUsernameCached(mikMapUser.id) } returns mikMapUser.username
        every { userService.getUsernameCached(zachUser.id) } returns zachUser.username

        val chartInfo = leaderboardService.dailyLeaderboardData(testUser.id)

        assertThat(chartInfo.labels).containsExactly(
            testUser.username,
            derekUser.username,
            jackieUser.username,
            chatGPTUser.username,
            mikMapUser.username
        )
        assertThat(chartInfo.dataPoints).containsExactly(10.0, 9.0, 8.0, 7.0, 6.0)
    }

    @Test
    fun `dailyLeaderboardData handles tied scores correctly`() {
        every { resultDAO.allResultsBetweenStream(any(), any()) } returns Stream.of(
            result.copy(userId = testUser.id, score = 10),
            result.copy(userId = derekUser.id, score = 10),
            result.copy(userId = jackieUser.id, score = 8),
            result.copy(userId = chatGPTUser.id, score = 7),
            result.copy(userId = mikMapUser.id, score = 6),
            result.copy(userId = zachUser.id, score = 5)
        )
        every { userService.getUsernameCached(testUser.id) } returns testUser.username
        every { userService.getUsernameCached(derekUser.id) } returns derekUser.username
        every { userService.getUsernameCached(jackieUser.id) } returns jackieUser.username
        every { userService.getUsernameCached(chatGPTUser.id) } returns chatGPTUser.username
        every { userService.getUsernameCached(mikMapUser.id) } returns mikMapUser.username
        every { userService.getUsernameCached(zachUser.id) } returns zachUser.username

        val chartInfo = leaderboardService.dailyLeaderboardData(testUser.id)

        assertThat(chartInfo.labels).containsExactly(
            testUser.username,
            derekUser.username,
            jackieUser.username,
            chatGPTUser.username,
            mikMapUser.username
        )
        assertThat(chartInfo.dataPoints).containsExactly(10.0, 10.0, 8.0, 7.0, 6.0)
    }
}
