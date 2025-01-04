package sh.zachwal.dailygames.home

import com.google.common.truth.Truth.assertThat
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import sh.zachwal.dailygames.db.jdbi.User
import sh.zachwal.dailygames.db.jdbi.puzzle.Game
import sh.zachwal.dailygames.db.jdbi.puzzle.PuzzleResult
import sh.zachwal.dailygames.leaderboard.PointCalculator
import sh.zachwal.dailygames.results.ResultService
import sh.zachwal.dailygames.results.resultinfo.TravleInfo
import sh.zachwal.dailygames.results.resultinfo.WorldleInfo
import java.time.Instant

class ShareTextServiceTest {

    private val resultService = mockk<ResultService>()
    private val shareLineMapper = ShareLineMapper(
        pointCalculator = PointCalculator()
    )

    private val service = ShareTextService(
        resultService = resultService,
        shareLineMapper = shareLineMapper,
        pointCalculator = PointCalculator(),
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

        val view = service.shareTextModalView(user)!!

        assertThat(view.shareTextLines).hasSize(3)
        assertThat(view.shareTextLines.take(2)).containsExactly(
            shareLineMapper.mapToShareLine(worldleResult),
            shareLineMapper.mapToShareLine(travleResult),
        )
    }

    @Test
    fun `ShareTextModal includes line for points`() {
        val user = User(id = 1L, username = "zach", hashedPassword = "123abc==")

        every { resultService.resultsForUserToday(user) } returns listOf(worldleResult, travleResult)

        val view = service.shareTextModalView(user)!!

        assertThat(view.shareTextLines).contains("Points: 3/12")
    }
}