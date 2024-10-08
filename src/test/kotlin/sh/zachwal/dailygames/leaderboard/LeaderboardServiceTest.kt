package sh.zachwal.dailygames.leaderboard

import com.google.common.truth.Truth.assertThat
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.jdbi.v3.sqlobject.kotlin.attach
import sh.zachwal.dailygames.db.dao.game.Top5DAO
import sh.zachwal.dailygames.db.dao.game.WorldleDAO
import sh.zachwal.dailygames.db.jdbi.User
import sh.zachwal.dailygames.db.jdbi.puzzle.Game
import sh.zachwal.dailygames.db.jdbi.puzzle.Top5Result
import sh.zachwal.dailygames.db.jdbi.puzzle.WorldleResult
import sh.zachwal.dailygames.nav.NavItem
import sh.zachwal.dailygames.nav.NavViewFactory
import sh.zachwal.dailygames.users.UserService
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.stream.Stream
import kotlin.test.Test

class LeaderboardServiceTest {
    private val top5DAO: Top5DAO = mockk()
    private val worldleDAO: WorldleDAO = mockk()

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
    private val navViewFactory = mockk<NavViewFactory>(relaxed = true)
    private val leaderboardService = LeaderboardService(
        userService = userService,
        jdbi = mockk {
            every { open() } returns mockk(relaxed = true) {
                every { attach(Top5DAO::class) } returns top5DAO
                every { attach(WorldleDAO::class) } returns worldleDAO
            }
        },
        pointCalculator = PuzzleResultPointCalculator(),
        navViewFactory = navViewFactory,
    )

    private val result = Top5Result(
        id = 1L,
        userId = 1L,
        game = Game.TOP5,
        score = 5,
        puzzleNumber = 30,
        puzzleDate = null,
        instantSubmitted = Instant.now(),
        shareText = "",
        numGuesses = 0,
        numCorrect = 0,
        isPerfect = false,
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
        every { top5DAO.allResultsStream() } returns Stream.of(
            result.copy(score = 5),
            result.copy(score = 4)
        )

        val leaderboardData = leaderboardService.gameLeaderboardData(testUser, Game.TOP5)

        assertThat(leaderboardData.allTimeAverage.labels).containsExactly(testUser.username)
        assertThat(leaderboardData.allTimeAverage.dataPoints).containsExactly(4.5)
    }

    @Test
    fun `gameLeaderboardData returns user's total points`() {
        every { top5DAO.allResultsStream() } returns Stream.of(
            result.copy(score = 5),
            result.copy(score = 4)
        )

        val leaderboardData = leaderboardService.gameLeaderboardData(testUser, Game.TOP5)

        assertThat(leaderboardData.allTimePoints.labels).containsExactly(testUser.username)
        assertThat(leaderboardData.allTimePoints.dataPoints).containsExactly(9.0)
    }

    @Test
    fun `gameLeaderboardData returns user's total games played`() {
        every { top5DAO.allResultsStream() } returns Stream.of(
            result.copy(score = 5),
            result.copy(score = 4)
        )

        val leaderboardData = leaderboardService.gameLeaderboardData(testUser, Game.TOP5)

        assertThat(leaderboardData.allTimeGames.labels).containsExactly(testUser.username)
        assertThat(leaderboardData.allTimeGames.dataPoints).containsExactly(2.0)
    }

    @Test
    fun `gameLeaderboardData filters to user's last thirty days`() {
        every { top5DAO.allResultsStream() } returns Stream.of(
            result.copy(score = 5, instantSubmitted = Instant.now()),
            result.copy(score = 4, instantSubmitted = Instant.now().minus(31, ChronoUnit.DAYS))
        )

        val leaderboardData = leaderboardService.gameLeaderboardData(testUser, Game.TOP5)

        assertThat(leaderboardData.thirtyDaysAverage.labels).containsExactly(testUser.username)
        assertThat(leaderboardData.thirtyDaysAverage.dataPoints).containsExactly(5.0)
    }

    @Test
    fun `gameLeaderboardData returns total in last thirty days`() {
        every { top5DAO.allResultsStream() } returns Stream.of(
            result.copy(score = 5, instantSubmitted = Instant.now()),
            result.copy(score = 6, instantSubmitted = Instant.now()),
            result.copy(score = 4, instantSubmitted = Instant.now().minus(31, ChronoUnit.DAYS))
        )

        val leaderboardData = leaderboardService.gameLeaderboardData(testUser, Game.TOP5)

        assertThat(leaderboardData.thirtyDaysPoints.labels).containsExactly(testUser.username)
        assertThat(leaderboardData.thirtyDaysPoints.dataPoints).containsExactly(11.0)
    }

    @Test
    fun `gameLeaderboardData returns total games in last thirty days`() {
        every { top5DAO.allResultsStream() } returns Stream.of(
            result.copy(score = 5, instantSubmitted = Instant.now()),
            result.copy(score = 6, instantSubmitted = Instant.now()),
            result.copy(score = 4, instantSubmitted = Instant.now().minus(31, ChronoUnit.DAYS))
        )

        val leaderboardData = leaderboardService.gameLeaderboardData(testUser, Game.TOP5)

        assertThat(leaderboardData.thirtyDaysGames.labels).containsExactly(testUser.username)
        assertThat(leaderboardData.thirtyDaysGames.dataPoints).containsExactly(2.0)
    }

    @Test
    fun `gameLeaderboardData fetches from correct DAO`() {
        val worldleResult = WorldleResult(
            id = 1L,
            userId = testUser.id,
            game = Game.WORLDLE,
            score = 4,
            puzzleNumber = 30,
            puzzleDate = null,
            instantSubmitted = Instant.now(),
            shareText = "",
            scorePercentage = 100,
        )
        every { top5DAO.allResultsStream() } returns Stream.empty()
        every { worldleDAO.allResultsStream() } returns Stream.of(
            worldleResult
        )

        val leaderboardData = leaderboardService.gameLeaderboardData(testUser, Game.WORLDLE)
        val expectedPoints = PuzzleResultPointCalculator().calculatePoints(worldleResult).toDouble()

        assertThat(leaderboardData.allTimeAverage.labels).containsExactly(testUser.username)
        assertThat(leaderboardData.allTimeAverage.dataPoints).containsExactly(expectedPoints)
    }

    @Test
    fun `gameLeaderboardData includes only the top 5 users all time`() {
        every { top5DAO.allResultsStream() } returns Stream.of(
            result.copy(userId = zachUser.id, score = 5),

            result.copy(userId = testUser.id, score = 10),
            result.copy(userId = derekUser.id, score = 9),
            result.copy(userId = jackieUser.id, score = 8),
            result.copy(userId = chatGPTUser.id, score = 7),
            result.copy(userId = mikMapUser.id, score = 6)
        )

        val leaderboardData = leaderboardService.gameLeaderboardData(testUser, Game.TOP5)

        assertThat(leaderboardData.allTimeAverage.labels).containsExactly(
            testUser.username,
            derekUser.username,
            jackieUser.username,
            chatGPTUser.username,
            mikMapUser.username
        )
        assertThat(leaderboardData.allTimeAverage.labels).doesNotContain(zachUser.username)
    }
}
