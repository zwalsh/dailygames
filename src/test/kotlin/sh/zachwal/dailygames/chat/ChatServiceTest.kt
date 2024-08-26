package sh.zachwal.dailygames.chat

import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.Test
import sh.zachwal.dailygames.db.jdbi.puzzle.Game


class ChatServiceTest {

    private val chatService = ChatService()

    @Test
    fun `includes ChatView with correct game`() {
        // Given
        val chatService = ChatService()
        val username = "test"
        val game = Game.FLAGLE
        val puzzleNumber = 1

        // When
        val chatView = chatService.chatView(username, game, puzzleNumber)

        // Then
        assertThat(chatView.game).isEqualTo(game)
    }
}