package sh.zachwal.dailygames.chat

import com.google.common.truth.Truth.assertThat
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.jdbi.v3.core.Jdbi
import org.jdbi.v3.sqlobject.kotlin.attach
import org.junit.jupiter.api.Test
import sh.zachwal.dailygames.chat.views.ChatItemView
import sh.zachwal.dailygames.chat.views.ResultItemView
import sh.zachwal.dailygames.db.dao.ChatDAO
import sh.zachwal.dailygames.db.dao.game.PuzzleDAO
import sh.zachwal.dailygames.db.jdbi.Chat
import sh.zachwal.dailygames.db.jdbi.puzzle.Game
import sh.zachwal.dailygames.db.jdbi.puzzle.Puzzle
import sh.zachwal.dailygames.db.jdbi.puzzle.WorldleResult
import sh.zachwal.dailygames.results.ResultService
import sh.zachwal.dailygames.results.displayTime
import sh.zachwal.dailygames.users.UserService
import java.time.Instant
import java.util.stream.Stream

class ChatServiceTest {

    private val resultService = mockk<ResultService> {
        every { allResultsForPuzzle(any()) } returns emptyList()
    }
    private val userService = mockk<UserService>()
    private val puzzleDAO = mockk<PuzzleDAO> {
        every { previousPuzzle(any(), any()) } returns null
        every { nextPuzzle(any(), any()) } returns null
    }
    private val chatDAO = mockk<ChatDAO> {
        every { chatsForPuzzleDescending(any()) } returns emptyList()
    }
    private val jdbi = mockk<Jdbi> {
        every { open() } returns mockk(relaxed = true) {
            every { attach<PuzzleDAO>() } returns puzzleDAO
        }
    }
    private val chatService = ChatService(
        jdbi,
        resultService,
        userService,
        puzzleDAO,
        chatDAO,
    )

    @Test
    fun `includes ChatView with correct game`() {
        // Given
        val username = "test"
        val game = Game.FLAGLE
        val puzzleNumber = 1
        every { resultService.allResultsForPuzzle(Puzzle(Game.FLAGLE, 1, null)) } returns emptyList()

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
        every { resultService.allResultsForPuzzle(any()) } returns emptyList()

        val chatView = chatService.chatViewLatest("test", Game.WORLDLE)

        assertThat(chatView.puzzleNumber).isEqualTo(3)
    }

    private val worldle943 = Puzzle(Game.WORLDLE, 943, null)
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

    @Test
    fun `returns chat view with list of results with earliest first`() {
        every { resultService.allResultsForPuzzle(worldle943) } returns listOf(
            worldleResult,
            worldleResult.copy(id = 2L, userId = 2L, score = 5, instantSubmitted = Instant.now().minusSeconds(1)),
            worldleResult.copy(id = 3L, userId = 3L, score = 5, instantSubmitted = Instant.now().minusSeconds(2)),
            worldleResult.copy(id = 4L, userId = 4L, score = 5, instantSubmitted = Instant.now().minusSeconds(3)),
        )
        every { userService.getUsernameCached(1L) } returns "user1"
        every { userService.getUsernameCached(2L) } returns "user2"
        every { userService.getUsernameCached(3L) } returns "user3"
        every { userService.getUsernameCached(4L) } returns "user4"

        val chatView = chatService.chatView("test", Game.WORLDLE, 943)

        assertThat(chatView.chatFeedItems).hasSize(4)
        val items = chatView.chatFeedItems as List<ResultItemView>
        assertThat(items[0].username).isEqualTo("user4")
        assertThat(items[1].username).isEqualTo("user3")
        assertThat(items[2].username).isEqualTo("user2")
        assertThat(items[3].username).isEqualTo("user1")
    }

    @Test
    fun `chat view items have correct username, result title, share text and timestamp`() {
        val shareText = "My test share text"
        every { resultService.allResultsForPuzzle(worldle943) } returns listOf(worldleResult.copy(shareText = shareText))
        every { userService.getUsernameCached(1L) } returns "user1"

        val chatView = chatService.chatView("test", Game.WORLDLE, 943)

        assertThat(chatView.chatFeedItems).hasSize(1)
        val items = chatView.chatFeedItems as List<ResultItemView>
        assertThat(items[0].username).isEqualTo("user1")
        assertThat(items[0].shareText).isEqualTo(shareText)
        assertThat(items[0].timestampText).isEqualTo(displayTime(worldleResult.instantSubmitted))
    }

    @Test
    fun `chat view includes previous link`() {
        every { puzzleDAO.previousPuzzle(Game.WORLDLE, 3) } returns Puzzle(Game.WORLDLE, 1, null)

        val chatView = chatService.chatView("test", Game.WORLDLE, 3)

        assertThat(chatView.prevLink).isEqualTo("/game/worldle/puzzle/1")
    }

    @Test
    fun `chat view includes next link`() {
        every { puzzleDAO.nextPuzzle(Game.WORLDLE, 3) } returns Puzzle(Game.WORLDLE, 10, null)

        val chatView = chatService.chatView("test", Game.WORLDLE, 3)

        assertThat(chatView.nextLink).isEqualTo("/game/worldle/puzzle/10")
    }

    @Test
    fun `chat view omits previous link if it is missing`() {
        every { puzzleDAO.previousPuzzle(any(), any()) } returns null

        val chatView = chatService.chatView("test", Game.WORLDLE, 1)

        assertThat(chatView.prevLink).isNull()
    }

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

        chatService.insertChat(1L, Game.WORLDLE, 123, "My chat!")

        verify { chatDAO.insertChat(1L, Puzzle(Game.WORLDLE, 123, null), "My chat!") }
    }

    @Test
    fun `includes chat item in feed`() {
        val chat = Chat(
            id = 1L,
            userId = 1L,
            game = Game.WORLDLE,
            puzzleNumber = 123,
            text = "My chat!",
            instantSubmitted = Instant.now(),
        )
        every { chatDAO.chatsForPuzzleDescending(Puzzle(Game.WORLDLE, 123, null)) } returns listOf(chat)
        every { userService.getUsernameCached(1L) } returns "user1"

        val chatView = chatService.chatView("test", Game.WORLDLE, 123)

        assertThat(chatView.chatFeedItems).hasSize(1)
        val item = chatView.chatFeedItems.single()
        assertThat(item).isInstanceOf(ChatItemView::class.java)
        val chatItem = item as ChatItemView
        assertThat(chatItem.username).isEqualTo("user1")
        assertThat(chatItem.text).isEqualTo("My chat!")
        assertThat(chatItem.timestampText).isEqualTo(displayTime(chat.instantSubmitted))
    }

    @Test
    fun `interleaves results and chats`() {
        val chat = Chat(
            id = 1L,
            userId = 1L,
            game = Game.WORLDLE,
            puzzleNumber = 123,
            text = "My chat!",
            instantSubmitted = Instant.now(),
        )
        val result = WorldleResult(
            id = 1L,
            userId = 1L,
            game = Game.WORLDLE,
            score = 5,
            puzzleNumber = 123,
            puzzleDate = null,
            instantSubmitted = Instant.now(),
            shareText = "",
            scorePercentage = 100,
        )
        every { chatDAO.chatsForPuzzleDescending(Puzzle(Game.WORLDLE, 123, null)) } returns listOf(chat)
        every { resultService.allResultsForPuzzle(Puzzle(Game.WORLDLE, 123, null)) } returns listOf(result)
        every { userService.getUsernameCached(1L) } returns "user1"

        val chatView = chatService.chatView("test", Game.WORLDLE, 123)

        assertThat(chatView.chatFeedItems).hasSize(2)
        val items = chatView.chatFeedItems
        assertThat(items[0]).isInstanceOf(ChatItemView::class.java)
        assertThat(items[1]).isInstanceOf(ResultItemView::class.java)
    }
}
