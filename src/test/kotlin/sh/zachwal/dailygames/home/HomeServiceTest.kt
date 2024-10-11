package sh.zachwal.dailygames.home

import com.google.common.truth.Truth.assertThat
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Test
import sh.zachwal.dailygames.db.jdbi.User
import sh.zachwal.dailygames.db.jdbi.puzzle.Game
import sh.zachwal.dailygames.db.jdbi.puzzle.TravleResult
import sh.zachwal.dailygames.db.jdbi.puzzle.WorldleResult
import sh.zachwal.dailygames.leaderboard.PuzzleResultPointCalculator
import sh.zachwal.dailygames.nav.NavViewFactory
import sh.zachwal.dailygames.results.ResultService
import java.time.Instant

class HomeServiceTest {

    private val resultService = mockk<ResultService> {
        every { resultFeed(any()) } returns emptyList()
        every { resultsForUserToday(any()) } returns emptyList()
    }
    private val shareLineMapper = ShareLineMapper()
    private val navViewFactory = mockk<NavViewFactory> {
        every { navView(any(), any()) } returns mockk()
    }

    private val homeService = HomeService(
        resultService = resultService,
        shareLineMapper = shareLineMapper,
        pointsCalculator = PuzzleResultPointCalculator(),
        navViewFactory = navViewFactory
    )

    private val worldleResult = WorldleResult(
        id = 1L,
        userId = 1L,
        game = Game.WORLDLE,
        score = 5,
        puzzleNumber = 943,
        puzzleDate = null,
        instantSubmitted = Instant.now(),
        shareText = "",
        scorePercentage = 100,
    )
    private val travleResult = TravleResult(
        id = 1L,
        userId = 1L,
        game = Game.TRAVLE,
        score = 5,
        puzzleNumber = 944,
        puzzleDate = null,
        instantSubmitted = Instant.now(),
        shareText = "",
        numGuesses = 10,
        numIncorrect = 2,
        numPerfect = 3,
        numHints = 1,
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
}
