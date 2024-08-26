package sh.zachwal.dailygames.chat

import com.google.common.truth.Truth.assertThat
import io.mockk.mockk
import org.junit.jupiter.api.Test
import sh.zachwal.dailygames.db.jdbi.puzzle.Game
import sh.zachwal.dailygames.results.ResultService


class ChatServiceTest {

    private val resultService = mockk<ResultService>()
    private val chatService = ChatService(resultService)

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
}