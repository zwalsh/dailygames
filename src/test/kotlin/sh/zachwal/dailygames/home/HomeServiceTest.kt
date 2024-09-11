package sh.zachwal.dailygames.home

import com.google.common.truth.Truth.assertThat
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import sh.zachwal.dailygames.db.jdbi.User
import sh.zachwal.dailygames.db.jdbi.puzzle.Game
import sh.zachwal.dailygames.db.jdbi.puzzle.TravleResult
import sh.zachwal.dailygames.db.jdbi.puzzle.WorldleResult
import sh.zachwal.dailygames.results.ResultService
import java.time.Instant

class HomeServiceTest {

    private val resultService = mockk<ResultService> {
        every { resultFeed(any()) } returns emptyList()
        every { resultsForUserToday(any()) } returns emptyList()
    }
    private val shareLineMapper = ShareLineMapper()
    private val homeService = HomeService(resultService, shareLineMapper)

    @Test
    fun `creates home view with username of user`() {
        val user = User(id = 1L, username = "zach", hashedPassword = "123abc==")
        val view = homeService.homeView(user)

        assertEquals(user.username, view.username)
    }

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
    fun `includes ShareTextModalView with a line per result`() {
        val user = User(id = 1L, username = "zach", hashedPassword = "123abc==")

        every { resultService.resultsForUserToday(user) } returns listOf(worldleResult, travleResult)

        val view = homeService.homeView(user)

        assertThat(view.shareTextModalView.shareTextLines).hasSize(2)
        assertThat(view.shareTextModalView.shareTextLines).containsExactly(
            shareLineMapper.mapToShareLine(worldleResult),
            shareLineMapper.mapToShareLine(travleResult),
        )
    }
}
