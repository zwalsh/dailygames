package sh.zachwal.dailygames.chat

import com.google.common.truth.Truth.assertThat
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Test
import sh.zachwal.dailygames.db.dao.ChatDAO
import sh.zachwal.dailygames.db.dao.game.PuzzleDAO
import sh.zachwal.dailygames.db.jdbi.Chat
import sh.zachwal.dailygames.db.jdbi.User
import sh.zachwal.dailygames.db.jdbi.puzzle.Game
import sh.zachwal.dailygames.db.jdbi.puzzle.Puzzle
import sh.zachwal.dailygames.utils.DisplayTimeService
import java.time.Instant

class ChatServiceTest {

    private val displayTimeService = mockk<DisplayTimeService> {
        every { displayTime(any(), any(), any()) } returns "Just now"
        every { longDisplayTime(any(), any()) } returns "Long time ago"
    }
    private val puzzleDAO = mockk<PuzzleDAO> {
        every { previousPuzzle(any(), any()) } returns null
        every { nextPuzzle(any(), any()) } returns null
        every { latestPuzzlePerGame() } returns emptyList()
    }
    private val chatDAO = mockk<ChatDAO> {
        every { chatsForPuzzleDescending(any()) } returns emptyList()
    }
    private val chatService = ChatService(
        displayTimeService = displayTimeService,
        puzzleDAO = puzzleDAO,
        chatDAO = chatDAO,
    )
    private val testUser = User(
        id = 1L,
        username = "test",
        hashedPassword = "hashedPassword",
    )

    @Test
    fun `can insert chat`() {
        every { chatDAO.insertChat(1L, Puzzle(Game.WORLDLE, 123, null), "My chat!") } returns Chat(
            id = 1L,
            userId = 1L,
            game = Game.WORLDLE,
            puzzleNumber = 123,
            text = "My chat!",
            instantSubmitted = Instant.now(),
        )

        val chatResponse = chatService.insertChat(testUser, Game.WORLDLE, 123, "My chat!")

        verify { chatDAO.insertChat(1L, Puzzle(Game.WORLDLE, 123, null), "My chat!") }

        assertThat(chatResponse.text).isEqualTo("My chat!")
        assertThat(chatResponse.username).isEqualTo(testUser.username)
    }

    @Test
    fun `currentChatCounts checks for the latest puzzles`() {
        chatService.currentChatCounts()

        verify { puzzleDAO.latestPuzzlePerGame() }
    }

    @Test
    fun `currentChatCounts returns the count for every game`() {
        val worldlePuzzle = Puzzle(Game.WORLDLE, 3, null)
        val flaglePuzzle = Puzzle(Game.FLAGLE, 4, null)
        val tradlePuzzle = Puzzle(Game.TRADLE, 5, null)
        val top5Puzzle = Puzzle(Game.TOP5, 6, null)
        val travlePuzzle = Puzzle(Game.TRAVLE, 7, null)
        every { puzzleDAO.latestPuzzlePerGame() } returns listOf(
            worldlePuzzle,
            flaglePuzzle,
            tradlePuzzle,
            top5Puzzle,
            travlePuzzle,
        )
        every { chatDAO.chatCountForPuzzle(worldlePuzzle) } returns 0
        every { chatDAO.chatCountForPuzzle(flaglePuzzle) } returns 5
        every { chatDAO.chatCountForPuzzle(tradlePuzzle) } returns 2
        every { chatDAO.chatCountForPuzzle(top5Puzzle) } returns 7
        every { chatDAO.chatCountForPuzzle(travlePuzzle) } returns 9

        val chatCounts = chatService.currentChatCounts()

        assertThat(chatCounts).containsExactlyEntriesIn(
            mapOf(
                Game.WORLDLE to 0,
                Game.FLAGLE to 5,
                Game.TRADLE to 2,
                Game.TOP5 to 7,
                Game.TRAVLE to 9,
            )
        )
    }
}
