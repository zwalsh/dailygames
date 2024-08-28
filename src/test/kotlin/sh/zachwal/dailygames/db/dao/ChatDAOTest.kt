package sh.zachwal.dailygames.db.dao

import com.google.common.truth.Truth.assertThat
import org.jdbi.v3.core.Jdbi
import org.jdbi.v3.sqlobject.kotlin.onDemand
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import sh.zachwal.dailygames.db.extension.DatabaseExtension
import sh.zachwal.dailygames.db.extension.Fixtures

@ExtendWith(DatabaseExtension::class)
class ChatDAOTest(
    jdbi: Jdbi,
    private val fixtures: Fixtures,
) {
    private val chatDAO: ChatDAO = jdbi.onDemand()

    @Test
    fun `can insert and retrieve chat`() {
        val chat = chatDAO.insertChat(
            userId = fixtures.zach.id,
            puzzle = fixtures.worldle123Puzzle,
            text = "Hello, world!",
        )

        val retrievedChat = chatDAO.chatsForPuzzleDescending(fixtures.worldle123Puzzle).single()

        assertThat(retrievedChat).isEqualTo(chat)
    }

    @Test
    fun `can list chats for a puzzle`() {
        val chat1 = chatDAO.insertChat(
            userId = fixtures.zach.id,
            puzzle = fixtures.worldle123Puzzle,
            text = "Hello, world!",
        )
        val chat2 = chatDAO.insertChat(
            userId = fixtures.jackie.id,
            puzzle = fixtures.worldle123Puzzle,
            text = "Hello, world!",
        )

        val chats = chatDAO.chatsForPuzzleDescending(fixtures.worldle123Puzzle)

        assertThat(chats).containsExactly(chat2, chat1).inOrder()
    }

    @Test
    fun `only includes chats for specific puzzle (game and number)`() {
        val chat1 = chatDAO.insertChat(
            userId = fixtures.zach.id,
            puzzle = fixtures.worldle123Puzzle,
            text = "Hello, world!",
        )

        val chat2 = chatDAO.insertChat(
            userId = fixtures.zach.id,
            puzzle = fixtures.flagle123Puzzle,
            text = "Hello, world!",
        )

        val chats = chatDAO.chatsForPuzzleDescending(fixtures.worldle123Puzzle)

        assertThat(chats).containsExactly(chat1)
        assertThat(chats).doesNotContain(chat2)
    }

    @Test
    fun `allChatsSinceInstantAscending returns all chats since the given instant`() {
        val chat1 = chatDAO.insertChat(
            userId = fixtures.zach.id,
            puzzle = fixtures.worldle123Puzzle,
            text = "Hello, world!",
        )
        Thread.sleep(1)

        val chat2 = chatDAO.insertChat(
            userId = fixtures.zach.id,
            puzzle = fixtures.flagle123Puzzle,
            text = "Hello, world!",
        )

        val chat3 = chatDAO.insertChat(
            userId = fixtures.zach.id,
            puzzle = fixtures.worldle123Puzzle,
            text = "Hello, world!",
        )

        val chats = chatDAO.allChatsSinceInstantAscending(chat1.instantSubmitted.plusMillis(1))

        assertThat(chats).containsExactly(chat2, chat3).inOrder()
    }
}
