package sh.zachwal.dailygames.chat

import com.google.common.truth.Truth.assertThat
import io.mockk.every
import io.mockk.mockk
import org.jdbi.v3.core.Jdbi
import org.jdbi.v3.sqlobject.kotlin.attach
import org.junit.jupiter.api.Test
import sh.zachwal.dailygames.db.dao.game.PuzzleDAO
import sh.zachwal.dailygames.db.jdbi.puzzle.Game
import sh.zachwal.dailygames.db.jdbi.puzzle.Puzzle
import sh.zachwal.dailygames.db.jdbi.puzzle.WorldleResult
import sh.zachwal.dailygames.results.ResultService
import sh.zachwal.dailygames.users.UserService
import java.time.Instant
import java.util.stream.Stream


class ChatServiceTest {

    private val resultService = mockk<ResultService>()
    private val userService = mockk<UserService>()
    private val puzzleDAO = mockk<PuzzleDAO>()
    private val jdbi = mockk<Jdbi> {
        every { open() } returns mockk(relaxed = true) {
            every { attach<PuzzleDAO>() } returns puzzleDAO
        }
    }
    private val chatService = ChatService(
        jdbi,
        resultService,
        userService
    )

    @Test
    fun `includes ChatView with correct game`() {
        // Given
        val username = "test"
        val game = Game.FLAGLE
        val puzzleNumber = 1

        // When
        val chatView = chatService.chatView(username, game, puzzleNumber)

        // Then
        assertThat(chatView.game).isEqualTo(game)
    }

    @Test
    fun `latest chat view uses latest puzzle for given game`() {
        every { puzzleDAO.listPuzzlesForGameDescending(Game.WORLDLE) } returns Stream.of(
            Puzzle(Game.WORLDLE, 3, null),
            Puzzle(Game.WORLDLE, 2, null),
            Puzzle(Game.WORLDLE, 1, null)
        )

        val chatView = chatService.chatViewLatest("test", Game.WORLDLE)

        assertThat(chatView.puzzleNumber).isEqualTo(3)
    }

    @Test
    fun `returns chat view with list of results with earliest first`() {
        val worldleResult = WorldleResult(
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

        every { resultService.allResultsForPuzzle(Puzzle(Game.WORLDLE, 943, null)) } returns listOf(
            worldleResult,
            worldleResult.copy(id = 2L, userId = 2L, score = 5, instantSubmitted = Instant.now().minusSeconds(1)),
            worldleResult.copy(id = 3L, userId = 3L, score = 5, instantSubmitted = Instant.now().minusSeconds(2)),
            worldleResult.copy(id = 4L, userId = 4L, score = 5, instantSubmitted = Instant.now().minusSeconds(3)),
        )
        val chatView = chatService.chatViewLatest("test", Game.WORLDLE)

        assertThat(chatView.chatFeedItems).hasSize(4)
        assertThat(chatView.chatFeedItems[0])
    }
}