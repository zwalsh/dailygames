package sh.zachwal.dailygames.home

import com.google.common.truth.Truth.assertThat
import io.mockk.every
import io.mockk.mockk
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
    private val user = User(id = 1L, username = "zach", hashedPassword = "123abc==")
    private val shareLineMapper = ShareLineMapper(
        pointCalculator = PointCalculator()
    )
    private val streakService = mockk<StreakService> {
        every { streakForUser(any()) } returns 3
    }

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
    private val resultService = mockk<ResultService> {
        every { resultsForUserToday(user) } returns listOf(worldleResult, travleResult)
    }

    private val service = ShareTextService(
        resultService = resultService,
        shareLineMapper = shareLineMapper,
        pointCalculator = PointCalculator(),
        streakService = streakService,
    )

    @Test
    fun `includes ShareTextModalView with a line per result plus points line and streak line`() {
        val view = service.shareTextModalView(user)!!

        assertThat(view.shareTextLines).hasSize(4)
        assertThat(view.shareTextLines.take(2)).containsExactly(
            shareLineMapper.mapToShareLine(worldleResult),
            shareLineMapper.mapToShareLine(travleResult),
        )
    }

    @Test
    fun `ShareTextModal includes line for points`() {
        val view = service.shareTextModalView(user)!!

        assertThat(view.shareTextLines).contains("Points: 3/12")
    }

    @Test
    fun `when streak is greater than or equal to STREAK_THRESHOLD, includes a line for streak`() {
        every { streakService.streakForUser(any()) } returns STREAK_THRESHOLD
        val view = service.shareTextModalView(user)!!

        assertThat(view.shareTextLines).contains("Streak: $STREAK_THRESHOLD\uD83D\uDD25")
    }

    @Test
    fun `when streak is less than STREAK_THRESHOLD, does not include a streak line`() {
        every { streakService.streakForUser(any()) } returns STREAK_THRESHOLD - 1

        val view = service.shareTextModalView(user)!!

        val containsStreakLine = view.shareTextLines.any {
            it.contains("Streak")
        }

        assertThat(containsStreakLine).isFalse()
    }
}
