package sh.zachwal.dailygames.wrapped

import com.google.common.truth.Truth.assertThat
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.jdbi.v3.core.Jdbi
import org.jdbi.v3.sqlobject.kotlin.attach
import org.junit.jupiter.api.Test
import sh.zachwal.dailygames.db.dao.game.PuzzleResultDAO
import sh.zachwal.dailygames.db.jdbi.User
import sh.zachwal.dailygames.db.jdbi.puzzle.Game
import sh.zachwal.dailygames.db.jdbi.puzzle.PuzzleResult
import sh.zachwal.dailygames.leaderboard.PointCalculator
import sh.zachwal.dailygames.results.resultinfo.GeocirclesInfo
import sh.zachwal.dailygames.results.resultinfo.PinpointInfo
import sh.zachwal.dailygames.results.resultinfo.Top5Info
import sh.zachwal.dailygames.results.resultinfo.WorldleInfo
import sh.zachwal.dailygames.users.UserPreferencesService
import sh.zachwal.dailygames.users.UserService
import java.time.Instant
import java.time.ZoneId
import java.time.temporal.ChronoUnit
import java.util.stream.Stream

class WrappedServiceTest {

    private val result = PuzzleResult(
        id = 1L,
        userId = 1L,
        game = Game.WORLDLE,
        puzzleNumber = 1,
        instantSubmitted = Instant.now(),
        puzzleDate = null,
        score = 5,
        shareText = "",
        resultInfo = WorldleInfo(
            percentage = 100,
        )
    )
    private val resultDAO = mockk<PuzzleResultDAO>()
    private val jdbi = mockk<Jdbi> {
        every { open() } returns mockk(relaxed = true) {
            every { attach<PuzzleResultDAO>() } returns resultDAO
        }
    }

    private val userService = mockk<UserService>()
    private val userPreferencesService = mockk<UserPreferencesService> {
        every { getTimeZoneCached(any()) } returns ZoneId.of("America/New_York")
    }
    private val service = WrappedService(
        jdbi = jdbi,
        calculator = PointCalculator(),
        userService = userService,
        userPreferencesService = userPreferencesService,
    )

    @Test
    fun `wrapped data queries for year start and end`() {
        // Uses eastern time
        every {
            resultDAO.allResultsBetweenStream(
                Instant.parse("2024-01-01T05:00:00Z"),
                Instant.parse("2025-01-01T05:00:00Z")
            )
        } returns Stream.empty()

        service.generateWrappedData(2024)

        // Verify that the DAO was called with the correct start and end times
        verify {
            resultDAO.allResultsBetweenStream(
                Instant.parse("2024-01-01T05:00:00Z"),
                Instant.parse("2025-01-01T05:00:00Z")
            )
        }
    }

    @Test
    fun `wrapped data includes games played for each user`() {
        every { resultDAO.allResultsBetweenStream(any(), any()) } returns Stream.of(
            result,
            result.copy(),
            result.copy(),
            result.copy(userId = 2),
            result.copy(userId = 2),
        )

        val wrappedData = service.generateWrappedData(2024)

        assertThat(wrappedData).hasSize(2)

        val userOne = wrappedData.single { it.userId == 1L }
        assertThat(userOne.totalGamesPlayed).isEqualTo(3)

        val userTwo = wrappedData.single { it.userId == 2L }
        assertThat(userTwo.totalGamesPlayed).isEqualTo(2)
    }

    @Test
    fun `wrapped data calculates games played by game`() {
        every { resultDAO.allResultsBetweenStream(any(), any()) } returns Stream.of(
            result,
            result.copy(),
            result.copy(),
            result.copy(game = Game.GEOCIRCLES, resultInfo = GeocirclesInfo),
            result.copy(game = Game.GEOCIRCLES, resultInfo = GeocirclesInfo),
        )

        val wrappedData = service.generateWrappedData(2024)

        val userOne = wrappedData.single { it.userId == 1L }
        assertThat(userOne.gamesPlayedByGame).containsExactly(
            Game.WORLDLE, 3,
            Game.GEOCIRCLES, 2,
        )
    }

    @Test
    fun `wrapped data calculates points by game`() {
        every { resultDAO.allResultsBetweenStream(any(), any()) } returns Stream.of(
            result,
            result.copy(score = 1),
            result.copy(score = 1),
            result.copy(game = Game.GEOCIRCLES, score = 5, resultInfo = GeocirclesInfo),
            result.copy(game = Game.GEOCIRCLES, score = 10, resultInfo = GeocirclesInfo),
        )

        val wrappedData = service.generateWrappedData(2024)

        val userOne = wrappedData.single { it.userId == 1L }
        assertThat(userOne.pointsByGame).containsExactly(
            Game.WORLDLE, 14,
            Game.GEOCIRCLES, 15,
        )
    }

    @Test
    fun `calculates total points`() {
        every { resultDAO.allResultsBetweenStream(any(), any()) } returns Stream.of(
            result,
            result.copy(score = 1),
            result.copy(score = 1),
            result.copy(game = Game.GEOCIRCLES, score = 5, resultInfo = GeocirclesInfo),
            result.copy(game = Game.GEOCIRCLES, score = 10, resultInfo = GeocirclesInfo),
        )

        val wrappedData = service.generateWrappedData(2024)

        val userOne = wrappedData.single { it.userId == 1L }
        assertThat(userOne.totalPoints).isEqualTo(29)
    }

    @Test
    fun `ranks players by games played`() {
        every { resultDAO.allResultsBetweenStream(any(), any()) } returns Stream.of(
            result,
            result.copy(),
            result.copy(userId = 2),
            result.copy(userId = 2),
            result.copy(userId = 2, game = Game.GEOCIRCLES, resultInfo = GeocirclesInfo),
            result.copy(userId = 3, game = Game.PINPOINT, resultInfo = PinpointInfo),
        )

        val wrappedData = service.generateWrappedData(2024)

        val userTwo = wrappedData.single { it.userId == 2L }
        assertThat(userTwo.totalGamesRank).isEqualTo(1)

        val userOne = wrappedData.single { it.userId == 1L }
        assertThat(userOne.totalGamesRank).isEqualTo(2)

        val userThree = wrappedData.single { it.userId == 3L }
        assertThat(userThree.totalGamesRank).isEqualTo(3)
    }

    @Test
    fun `ranks players by total points`() {
        every { resultDAO.allResultsBetweenStream(any(), any()) } returns Stream.of(
            // user 1 has 12 points (from Worldle)
            result,
            result.copy(score = 1),
            result.copy(score = 1),
            // user 2 has 15 points (from Geocircles)
            result.copy(userId = 2, game = Game.GEOCIRCLES, score = 5, resultInfo = GeocirclesInfo),
            result.copy(userId = 2, game = Game.GEOCIRCLES, score = 10, resultInfo = GeocirclesInfo),
            // user 3 has 1 point (from Pinpoint)
            result.copy(userId = 3, game = Game.PINPOINT, score = 5, resultInfo = PinpointInfo),
        )

        val wrappedData = service.generateWrappedData(2024)

        val userTwo = wrappedData.single { it.userId == 2L }
        assertThat(userTwo.totalPointsRank).isEqualTo(1)

        val userOne = wrappedData.single { it.userId == 1L }
        assertThat(userOne.totalPointsRank).isEqualTo(2)

        val userThree = wrappedData.single { it.userId == 3L }
        assertThat(userThree.totalPointsRank).isEqualTo(3)
    }

    @Test
    fun `calculates a user's best day`() {
        val yesterday = Instant.now().minus(1, ChronoUnit.DAYS)
        every { resultDAO.allResultsBetweenStream(any(), any()) } returns Stream.of(
            result,
            result.copy(score = 1),
            result.copy(score = 1),
            result.copy(score = 1, instantSubmitted = yesterday),
            result.copy(score = 1, instantSubmitted = yesterday),
            result.copy(score = 1, instantSubmitted = yesterday),
            result.copy(score = 1, instantSubmitted = yesterday),
            result.copy(score = 1, instantSubmitted = yesterday),
            result.copy(score = 1, instantSubmitted = yesterday),
            result.copy(score = 1, instantSubmitted = yesterday),
            result.copy(score = 1, instantSubmitted = yesterday),
            result.copy(score = 1, instantSubmitted = yesterday),
        )

        val wrappedData = service.generateWrappedData(2024)

        val userOne = wrappedData.single { it.userId == 1L }
        assertThat(userOne.bestDay).isEqualTo(yesterday.atZone(ZoneId.of("America/New_York")).toLocalDate())
        assertThat(userOne.bestDayPoints).isEqualTo(54)
    }

    @Test
    fun `uses a user's local timezone`() {
        val todayMidnightPT = Instant.ofEpochSecond(1734854400)
        every { userPreferencesService.getTimeZoneCached(1L) } returns ZoneId.of("America/Los_Angeles")
        every { resultDAO.allResultsBetweenStream(any(), any()) } returns Stream.of(
            result.copy(instantSubmitted = todayMidnightPT.minus(4, ChronoUnit.HOURS)), // yesterday ET, PT
            result.copy(instantSubmitted = todayMidnightPT.minus(2, ChronoUnit.HOURS)), // today ET, yesterday PT
            result.copy(instantSubmitted = todayMidnightPT.minus(1, ChronoUnit.HOURS)), // today ET, yesterday PT
        )

        val wrappedData = service.generateWrappedData(2024)
        val userOne = wrappedData.single { it.userId == 1L }
        // yesterday local date
        val yesterday = todayMidnightPT.minusSeconds(1).atZone(ZoneId.of("America/Los_Angeles")).toLocalDate()
        assertThat(userOne.bestDay).isEqualTo(yesterday)
    }

    @Test
    fun `calculates total minutes a player played`() {
        every { resultDAO.allResultsBetweenStream(any(), any()) } returns Stream.of(
            result.copy(instantSubmitted = Instant.now().minus(1, ChronoUnit.DAYS)),
            result.copy(instantSubmitted = Instant.now().minus(1, ChronoUnit.DAYS).plusSeconds(120)),
            result.copy(instantSubmitted = Instant.now().minus(1, ChronoUnit.DAYS).plusSeconds(180)),
        )

        val wrappedData = service.generateWrappedData(2024)

        val userOne = wrappedData.single { it.userId == 1L }
        assertThat(userOne.totalMinutes).isEqualTo(3)
    }

    @Test
    fun `calculates total minutes, skipping gaps of larger than 20 minutes`() {
        every { resultDAO.allResultsBetweenStream(any(), any()) } returns Stream.of(
            result.copy(instantSubmitted = Instant.now().minus(1, ChronoUnit.DAYS)),
            result.copy(instantSubmitted = Instant.now().minus(1, ChronoUnit.DAYS).plus(1, ChronoUnit.MINUTES)),
            result.copy(instantSubmitted = Instant.now().minus(1, ChronoUnit.DAYS).plus(21, ChronoUnit.MINUTES)),
            result.copy(instantSubmitted = Instant.now().minus(1, ChronoUnit.DAYS).plus(22, ChronoUnit.MINUTES)),
        )

        val wrappedData = service.generateWrappedData(2024)

        val userOne = wrappedData.single { it.userId == 1L }
        assertThat(userOne.totalMinutes).isEqualTo(2)
    }

    @Test
    fun `ranks users by total minutes`() {
        every { resultDAO.allResultsBetweenStream(any(), any()) } returns Stream.of(
            result.copy(instantSubmitted = Instant.now().minus(1, ChronoUnit.MINUTES)),
            result,
            result.copy(userId = 2, instantSubmitted = Instant.now().minus(2, ChronoUnit.MINUTES)),
            result.copy(userId = 2),
        )

        val wrappedData = service.generateWrappedData(2024)

        val userOne = wrappedData.single { it.userId == 1L }
        assertThat(userOne.totalMinutesRank).isEqualTo(2)

        val userTwo = wrappedData.single { it.userId == 2L }
        assertThat(userTwo.totalMinutesRank).isEqualTo(1)
    }

    @Test
    fun `calculates most played game`() {
        every { resultDAO.allResultsBetweenStream(any(), any()) } returns Stream.of(
            result,
            result.copy(),
            result.copy(),
            result.copy(game = Game.GEOCIRCLES, resultInfo = GeocirclesInfo),
            result.copy(game = Game.GEOCIRCLES, resultInfo = GeocirclesInfo),
            result.copy(game = Game.PINPOINT, resultInfo = PinpointInfo),
        )

        val wrappedData = service.generateWrappedData(2024)

        val userOne = wrappedData.single { it.userId == 1L }
        assertThat(userOne.favoriteGame).isEqualTo(Game.WORLDLE)
    }

    @Test
    fun `calculates each user's most played game`() {
        every { resultDAO.allResultsBetweenStream(any(), any()) } returns Stream.of(
            result,
            result.copy(userId = 2),
            result.copy(userId = 3),
            result.copy(userId = 2, game = Game.GEOCIRCLES, resultInfo = GeocirclesInfo),
            result.copy(userId = 2, game = Game.GEOCIRCLES, resultInfo = GeocirclesInfo),
            result.copy(userId = 3, game = Game.PINPOINT, resultInfo = PinpointInfo),
        )

        val wrappedData = service.generateWrappedData(2024)

        val userOne = wrappedData.single { it.userId == 1L }
        assertThat(userOne.favoriteGame).isEqualTo(Game.WORLDLE)

        val userTwo = wrappedData.single { it.userId == 2L }
        assertThat(userTwo.favoriteGame).isEqualTo(Game.GEOCIRCLES)

        val userThree = wrappedData.single { it.userId == 3L }
        assertThat(userThree.favoriteGame).isAnyOf(Game.WORLDLE, Game.GEOCIRCLES, Game.PINPOINT)
    }

    @Test
    fun `calculates averages by game rounded to one decimal`() {
        every { resultDAO.allResultsBetweenStream(any(), any()) } returns Stream.of(
            result,
            result.copy(score = 1),
            result.copy(score = 1),
            result.copy(game = Game.GEOCIRCLES, score = 5, resultInfo = GeocirclesInfo),
            result.copy(game = Game.GEOCIRCLES, score = 10, resultInfo = GeocirclesInfo),
        )

        val wrappedData = service.generateWrappedData(2024)

        val userOne = wrappedData.single { it.userId == 1L }
        assertThat(userOne.averagesByGame).containsAtLeast(
            Game.WORLDLE, 4.7,
            Game.GEOCIRCLES, 7.5,
        )
    }

    @Test
    fun `calculates player's rank per game by total points`() {
        every { resultDAO.allResultsBetweenStream(any(), any()) } returns Stream.of(
            result,
            result.copy(score = 1),
            result.copy(userId = 2, score = 1),
            result.copy(game = Game.GEOCIRCLES, score = 5, resultInfo = GeocirclesInfo),
            result.copy(userId = 2, game = Game.GEOCIRCLES, score = 10, resultInfo = GeocirclesInfo),
        )

        val wrappedData = service.generateWrappedData(2024)

        val userOne = wrappedData.single { it.userId == 1L }
        assertThat(userOne.ranksPerGameTotal).containsAtLeast(
            Game.WORLDLE, 1,
            Game.GEOCIRCLES, 2
        )

        val userTwo = wrappedData.single { it.userId == 2L }
        assertThat(userTwo.ranksPerGameTotal).containsAtLeast(
            Game.WORLDLE, 2,
            Game.GEOCIRCLES, 1
        )
    }

    @Test
    fun `calculates player's rank per game by average points`() {
        // player one's total is 200, average is 2
        val playerOneWorldle = List(100) {
            result
        }
        // player two's total is 60, average is 6
        val playerTwoWorldle = List(10) {
            result.copy(userId = 2, score = 1)
        }

        every { resultDAO.allResultsBetweenStream(any(), any()) } returns
            playerOneWorldle
                .plus(playerTwoWorldle)
                .stream()

        val wrappedData = service.generateWrappedData(2024)

        val userOne = wrappedData.single { it.userId == 1L }
        assertThat(userOne.ranksPerGameAverage).containsAtLeast(
            Game.WORLDLE, 2
        )
        val userTwo = wrappedData.single { it.userId == 2L }
        assertThat(userTwo.ranksPerGameAverage).containsAtLeast(
            Game.WORLDLE, 1
        )
    }

    @Test
    fun `user ranks do not include games where the user played less than 10 games`() {
        every { resultDAO.allResultsBetweenStream(any(), any()) } returns Stream.of(
            result
        )

        val wrappedData = service.generateWrappedData(2024)

        val userOne = wrappedData.single { it.userId == 1L }
        assertThat(userOne.ranksPerGameAverage).doesNotContainKey(Game.WORLDLE)
    }

    @Test
    fun `calculate a user's best game (highest rank by average)`() {
        val results = listOf(
            // user 3 is third at Worldle
            result.copy(userId = 1, game = Game.WORLDLE, score = 1),
            result.copy(userId = 2, game = Game.WORLDLE, score = 2),
            result.copy(userId = 3, game = Game.WORLDLE, score = 3),
            // user 3 is third at Geocircles
            result.copy(userId = 1, game = Game.GEOCIRCLES, score = 10, resultInfo = GeocirclesInfo),
            result.copy(userId = 2, game = Game.GEOCIRCLES, score = 7, resultInfo = GeocirclesInfo),
            result.copy(userId = 3, game = Game.GEOCIRCLES, score = 5, resultInfo = GeocirclesInfo),
            // user 3 is second at Top5
            result.copy(userId = 1, game = Game.TOP5, score = 10, resultInfo = Top5Info(5, 5, false)),
            result.copy(userId = 2, game = Game.TOP5, score = 5, resultInfo = Top5Info(5, 5, false)),
            result.copy(userId = 3, game = Game.TOP5, score = 9, resultInfo = Top5Info(5, 5, false)),
        )

        every { resultDAO.allResultsBetweenStream(any(), any()) } returns
            // repeat 10 times to exceed minimum cutoff
            List(10) { results }
                .flatten()
                .stream()

        val wrappedData = service.generateWrappedData(2024)

        val userThree = wrappedData.single { it.userId == 3L }
        assertThat(userThree.bestGame).isEqualTo(Game.TOP5)
        assertThat(userThree.ranksPerGameAverage[Game.TOP5]).isEqualTo(2)
    }

    @Test
    fun `best game is null if user does not qualify for any game`() {
        every { resultDAO.allResultsBetweenStream(any(), any()) } returns Stream.of(
            result
        )

        val wrappedData = service.generateWrappedData(2024)

        val userOne = wrappedData.single { it.userId == 1L }
        assertThat(userOne.bestGame).isNull()
    }

    @Test
    fun `calculates a user's longest streak`() {
        every { resultDAO.allResultsBetweenStream(any(), any()) } returns Stream.of(
            result,
            result.copy(instantSubmitted = Instant.now().plus(1, ChronoUnit.DAYS)),
            result.copy(instantSubmitted = Instant.now().plus(2, ChronoUnit.DAYS)),
            result.copy(instantSubmitted = Instant.now().plus(3, ChronoUnit.DAYS)),
            // gap
            result.copy(instantSubmitted = Instant.now().plus(5, ChronoUnit.DAYS)),
            result.copy(instantSubmitted = Instant.now().plus(6, ChronoUnit.DAYS)),
            result.copy(instantSubmitted = Instant.now().plus(7, ChronoUnit.DAYS)),
            result.copy(instantSubmitted = Instant.now().plus(8, ChronoUnit.DAYS)),
            result.copy(instantSubmitted = Instant.now().plus(9, ChronoUnit.DAYS)),
        )

        val wrappedData = service.generateWrappedData(2024)

        val userOne = wrappedData.single { it.userId == 1L }
        assertThat(userOne.longestStreak).isEqualTo(5)
        assertThat(userOne.longestStreakGame).isEqualTo(Game.WORLDLE)
    }

    @Test
    fun `calculates longest streak even if different games are interleaved`() {
        every { resultDAO.allResultsBetweenStream(any(), any()) } returns Stream.of(
            result,
            result.copy(instantSubmitted = Instant.now().plus(1, ChronoUnit.DAYS)),
            result.copy(instantSubmitted = Instant.now().plus(1, ChronoUnit.DAYS), game = Game.GEOCIRCLES, resultInfo = GeocirclesInfo),
            result.copy(instantSubmitted = Instant.now().plus(2, ChronoUnit.DAYS)),
            result.copy(instantSubmitted = Instant.now().plus(2, ChronoUnit.DAYS), game = Game.GEOCIRCLES, resultInfo = GeocirclesInfo),
            result.copy(instantSubmitted = Instant.now().plus(3, ChronoUnit.DAYS)),
            result.copy(instantSubmitted = Instant.now().plus(3, ChronoUnit.DAYS), game = Game.GEOCIRCLES, resultInfo = GeocirclesInfo),
            result.copy(instantSubmitted = Instant.now().plus(4, ChronoUnit.DAYS)),
            result.copy(instantSubmitted = Instant.now().plus(4, ChronoUnit.DAYS), game = Game.GEOCIRCLES, resultInfo = GeocirclesInfo),
        )

        val wrappedData = service.generateWrappedData(2024)

        val userOne = wrappedData.single { it.userId == 1L }
        assertThat(userOne.longestStreak).isEqualTo(5)
        assertThat(userOne.longestStreakGame).isEqualTo(Game.WORLDLE)
    }

    @Test
    fun `calculates longest streak for multiple users`() {
        every { resultDAO.allResultsBetweenStream(any(), any()) } returns Stream.of(
            result,
            result.copy(instantSubmitted = Instant.now().plus(1, ChronoUnit.DAYS)),
            result.copy(instantSubmitted = Instant.now().plus(2, ChronoUnit.DAYS)),
            result.copy(instantSubmitted = Instant.now().plus(3, ChronoUnit.DAYS)),
            // gap
            result.copy(instantSubmitted = Instant.now().plus(5, ChronoUnit.DAYS)),
            result.copy(instantSubmitted = Instant.now().plus(6, ChronoUnit.DAYS)),
            result.copy(instantSubmitted = Instant.now().plus(7, ChronoUnit.DAYS)),
            result.copy(instantSubmitted = Instant.now().plus(8, ChronoUnit.DAYS)),
            result.copy(instantSubmitted = Instant.now().plus(9, ChronoUnit.DAYS)),
            result.copy(userId = 2),
            result.copy(userId = 2, instantSubmitted = Instant.now().plus(1, ChronoUnit.DAYS)),
            result.copy(userId = 2, instantSubmitted = Instant.now().plus(2, ChronoUnit.DAYS)),
            result.copy(userId = 2, instantSubmitted = Instant.now().plus(3, ChronoUnit.DAYS)),
            // gap
            result.copy(userId = 2, instantSubmitted = Instant.now().plus(5, ChronoUnit.DAYS)),
            result.copy(userId = 2, instantSubmitted = Instant.now().plus(6, ChronoUnit.DAYS)),
            result.copy(userId = 2, instantSubmitted = Instant.now().plus(7, ChronoUnit.DAYS)),
            result.copy(userId = 2, instantSubmitted = Instant.now().plus(8, ChronoUnit.DAYS)),
            result.copy(userId = 2, instantSubmitted = Instant.now().plus(9, ChronoUnit.DAYS)),
        )

        val wrappedData = service.generateWrappedData(2024)

        val userOne = wrappedData.single { it.userId == 1L }
        assertThat(userOne.longestStreak).isEqualTo(5)
        assertThat(userOne.longestStreakGame).isEqualTo(Game.WORLDLE)

        val userTwo = wrappedData.single { it.userId == 2L }
        assertThat(userTwo.longestStreak).isEqualTo(5)
        assertThat(userTwo.longestStreakGame).isEqualTo(Game.WORLDLE)
    }

    @Test
    fun `caches wrapped data for one user`() {
        every { resultDAO.allResultsBetweenStream(any(), any()) } returns Stream.of(
            result
        )

        val currentUser = User(1, "name", "password")
        service.wrappedView(2024, currentUser)
        service.wrappedView(2024, currentUser)

        verify(exactly = 1) {
            resultDAO.allResultsBetweenStream(any(), any())
        }
    }

    @Test
    fun `caches wrapped data for two users in one year`() {
        every { resultDAO.allResultsBetweenStream(any(), any()) } returns Stream.of(
            result,
            result.copy(userId = 2),
        )

        val userOne = User(1, "name", "password")
        val userTwo = User(2, "name", "password")
        service.wrappedView(2024, userOne)
        service.wrappedView(2024, userTwo)
        service.wrappedView(2024, userOne)
        service.wrappedView(2024, userTwo)

        verify(exactly = 1) {
            resultDAO.allResultsBetweenStream(any(), any())
        }
    }
}
