package sh.zachwal.dailygames.wrapped

import com.google.common.truth.Truth.assertThat
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.jdbi.v3.core.Jdbi
import org.jdbi.v3.sqlobject.kotlin.attach
import org.junit.jupiter.api.Test
import sh.zachwal.dailygames.db.dao.game.PuzzleResultDAO
import sh.zachwal.dailygames.db.jdbi.puzzle.Game
import sh.zachwal.dailygames.db.jdbi.puzzle.PuzzleResult
import sh.zachwal.dailygames.leaderboard.PointCalculator
import sh.zachwal.dailygames.results.resultinfo.GeocirclesInfo
import sh.zachwal.dailygames.results.resultinfo.PinpointInfo
import sh.zachwal.dailygames.results.resultinfo.TravleInfo
import sh.zachwal.dailygames.results.resultinfo.WorldleInfo
import java.time.Instant
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

    private val service = WrappedService(
        jdbi = jdbi,
        calculator = PointCalculator(),
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
}
