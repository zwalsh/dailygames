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
import sh.zachwal.dailygames.results.ResultService
import java.util.stream.Stream


class ChatServiceTest {

    private val resultService = mockk<ResultService>()
    private val puzzleDAO = mockk<PuzzleDAO>()
    private val jdbi = mockk<Jdbi> {
        every { open() } returns mockk(relaxed = true) {
            every { attach<PuzzleDAO>() } returns puzzleDAO
        }
    }
    private val chatService = ChatService(
        jdbi,
        resultService
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
}