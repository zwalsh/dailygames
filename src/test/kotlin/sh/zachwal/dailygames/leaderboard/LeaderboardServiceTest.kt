package sh.zachwal.dailygames.leaderboard

import com.google.common.truth.Truth.assertThat
import io.mockk.every
import io.mockk.mockk
import org.jdbi.v3.sqlobject.kotlin.attach
import sh.zachwal.dailygames.db.dao.game.Top5DAO
import sh.zachwal.dailygames.db.dao.game.WorldleDAO
import sh.zachwal.dailygames.db.jdbi.User
import sh.zachwal.dailygames.db.jdbi.puzzle.Game
import sh.zachwal.dailygames.db.jdbi.puzzle.Top5Result
import sh.zachwal.dailygames.db.jdbi.puzzle.WorldleResult
import sh.zachwal.dailygames.nav.LeaderboardNavItemView
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
    private val leaderboardService = LeaderboardService(
        userService = userService,
        jdbi = mockk {
            every { open() } returns mockk(relaxed = true) {
                every { attach(Top5DAO::class) } returns top5DAO
                every { attach(WorldleDAO::class) } returns worldleDAO
            }
        },
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
    fun `leaderboardView returns LeaderboardView with Nav set to LEADERBOARD`() {
        val leaderboardView = leaderboardService.overallLeaderboardView(testUser)

        val navItem = leaderboardView.nav.navItems[1]

        assertThat(navItem).isInstanceOf(LeaderboardNavItemView::class.java)

        val leaderboardNavItemView = navItem as LeaderboardNavItemView
        assertThat(leaderboardNavItemView.isActive).isTrue()
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

        assertThat(leaderboardData.allTime.labels).containsExactly(testUser.username)
        assertThat(leaderboardData.allTime.dataPoints).containsExactly(4.5)
    }

    @Test
    fun `gameLeaderboardData filters to user's last thirty days`() {
        every { top5DAO.allResultsStream() } returns Stream.of(
            result.copy(score = 5, instantSubmitted = Instant.now()),
            result.copy(score = 4, instantSubmitted = Instant.now().minus(31, ChronoUnit.DAYS))
        )

        val leaderboardData = leaderboardService.gameLeaderboardData(testUser, Game.TOP5)

        assertThat(leaderboardData.past30Days.labels).containsExactly(testUser.username)
        assertThat(leaderboardData.past30Days.dataPoints).containsExactly(5.0)
    }

    @Test
    fun `gameLeaderboardData fetches from correct DAO`() {
        every { top5DAO.allResultsStream() } returns Stream.empty()
        every { worldleDAO.allResultsStream() } returns Stream.of(
            WorldleResult(
                id = 1L,
                userId = testUser.id,
                game = Game.WORLDLE,
                score = 10,
                puzzleNumber = 30,
                puzzleDate = null,
                instantSubmitted = Instant.now(),
                shareText = "",
                scorePercentage = 100,
            )
        )

        val leaderboardData = leaderboardService.gameLeaderboardData(testUser, Game.WORLDLE)

        assertThat(leaderboardData.allTime.labels).containsExactly(testUser.username)
        assertThat(leaderboardData.allTime.dataPoints).containsExactly(10.0)
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

        assertThat(leaderboardData.allTime.labels).containsExactly(
            testUser.username,
            derekUser.username,
            jackieUser.username,
            chatGPTUser.username,
            mikMapUser.username
        )
        assertThat(leaderboardData.allTime.labels).doesNotContain(zachUser.username)
    }
}
