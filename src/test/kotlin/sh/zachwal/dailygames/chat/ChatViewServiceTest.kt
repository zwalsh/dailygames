package sh.zachwal.dailygames.chat

import com.google.common.truth.Truth.assertThat
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.jdbi.v3.core.Jdbi
import org.jdbi.v3.sqlobject.kotlin.attach
import org.junit.jupiter.api.Test
import sh.zachwal.dailygames.answers.AnswerService
import sh.zachwal.dailygames.chat.views.ChatItemView
import sh.zachwal.dailygames.chat.views.ChatNav
import sh.zachwal.dailygames.chat.views.HiddenChatItemView
import sh.zachwal.dailygames.chat.views.ResultItemView
import sh.zachwal.dailygames.db.dao.ChatDAO
import sh.zachwal.dailygames.db.dao.game.PuzzleDAO
import sh.zachwal.dailygames.db.jdbi.Chat
import sh.zachwal.dailygames.db.jdbi.User
import sh.zachwal.dailygames.db.jdbi.puzzle.Game
import sh.zachwal.dailygames.db.jdbi.puzzle.Puzzle
import sh.zachwal.dailygames.db.jdbi.puzzle.PuzzleResult
import sh.zachwal.dailygames.nav.NavViewFactory
import sh.zachwal.dailygames.results.ResultService
import sh.zachwal.dailygames.results.resultinfo.WorldleInfo
import sh.zachwal.dailygames.users.UserService
import sh.zachwal.dailygames.utils.DisplayTimeService
import java.time.Clock
import java.time.Instant
import java.time.ZoneId
import java.util.stream.Stream

class ChatViewServiceTest {

    private val testUser = User(
        id = 1L,
        username = "test",
        hashedPassword = "hashedPassword",
    )
    private val zach = User(
        id = 2L,
        username = "zach",
        hashedPassword = "hashedPassword",
    )

    private val resultService = mockk<ResultService> {
        every { allResultsForPuzzle(any()) } returns emptyList()
    }
    private val userService = mockk<UserService> {
        every { getUsernameCached(testUser.id) } returns "test"
        every { getUsernameCached(zach.id) } returns "zach"
    }
    private val displayTimeService = mockk<DisplayTimeService> {
        every { displayTime(any(), any(), any()) } returns "Just now"
        every { longDisplayTime(any(), any()) } returns "Long time ago"
    }
    private val navViewFactory = NavViewFactory(mockk<ChatService>(relaxed = true))
    private val puzzleDAO = mockk<PuzzleDAO> {
        every { previousPuzzle(any(), any()) } returns null
        every { nextPuzzle(any(), any()) } returns null
        every { latestPuzzlePerGame() } returns emptyList()
    }
    private val chatDAO = mockk<ChatDAO> {
        every { chatsForPuzzleDescending(any()) } returns emptyList()
    }
    private val jdbi = mockk<Jdbi> {
        every { open() } returns mockk(relaxed = true) {
            every { attach<PuzzleDAO>() } returns puzzleDAO
        }
    }
    private val clock = Clock.fixed(Instant.now(), ZoneId.of("UTC"))
    private val answerService = mockk<AnswerService> {
        every { answerForPuzzle(any()) } returns null
    }
    private val chatService = ChatViewService(
        jdbi = jdbi,
        resultService = resultService,
        userService = userService,
        displayTimeService = displayTimeService,
        answerService = answerService,
        navViewFactory = navViewFactory,
        puzzleDAO = puzzleDAO,
        chatDAO = chatDAO,
        clock = clock,
    )

    @Test
    fun `includes ChatView with correct game`() {
        // Given
        val game = Game.FLAGLE
        val puzzleNumber = 1
        every { resultService.allResultsForPuzzle(Puzzle(Game.FLAGLE, 1, null)) } returns emptyList()

        // When
        val chatView = chatService.chatView(testUser, game, puzzleNumber)

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

        val chatView = chatService.chatViewLatest(testUser, Game.WORLDLE)

        assertThat(chatView.puzzleNumber).isEqualTo(3)
    }

    private val worldle943 = Puzzle(Game.WORLDLE, 943, null)
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
        )
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

        val chatView = chatService.chatView(testUser, Game.WORLDLE, 943)

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

        val chatView = chatService.chatView(testUser, Game.WORLDLE, 943)

        assertThat(chatView.chatFeedItems).hasSize(1)
        val items = chatView.chatFeedItems as List<ResultItemView>
        assertThat(items[0].username).isEqualTo("user1")
        assertThat(items[0].shareText).isEqualTo(shareText)
    }

    @Test
    fun `chat view includes previous link`() {
        every { puzzleDAO.previousPuzzle(Game.WORLDLE, 3) } returns Puzzle(Game.WORLDLE, 1, null)

        val chatView = chatService.chatView(testUser, Game.WORLDLE, 3)

        val chatNav = chatView.navView.insideNavItem as ChatNav
        assertThat(chatNav.prevLink).isEqualTo("/game/worldle/puzzle/1")
    }

    @Test
    fun `chat view includes next link`() {
        every { puzzleDAO.nextPuzzle(Game.WORLDLE, 3) } returns Puzzle(Game.WORLDLE, 10, null)

        val chatView = chatService.chatView(testUser, Game.WORLDLE, 3)
        val chatNav = chatView.navView.insideNavItem as ChatNav

        assertThat(chatNav.nextLink).isEqualTo("/game/worldle/puzzle/10")
    }

    @Test
    fun `chat view omits previous link if it is missing`() {
        every { puzzleDAO.previousPuzzle(any(), any()) } returns null

        val chatView = chatService.chatView(testUser, Game.WORLDLE, 1)
        val chatNav = chatView.navView.insideNavItem as ChatNav

        assertThat(chatNav.prevLink).isNull()
    }

    @Test
    fun `includes chat item in feed (when user has submitted a result)`() {
        val chat = Chat(
            id = 1L,
            userId = 2L,
            game = Game.WORLDLE,
            puzzleNumber = 123,
            text = "My chat!",
            instantSubmitted = Instant.now(),
        )
        every { chatDAO.chatsForPuzzleDescending(Puzzle(Game.WORLDLE, 123, null)) } returns listOf(chat)
        val currentUserResult = worldleResult.copy(userId = testUser.id)
        every { resultService.allResultsForPuzzle(Puzzle(Game.WORLDLE, 123, null)) } returns listOf(currentUserResult)
        every { userService.getUsernameCached(1L) } returns testUser.username
        every { userService.getUsernameCached(2L) } returns "user1"

        val chatView = chatService.chatView(testUser, Game.WORLDLE, 123)

        // Result & chat are present
        assertThat(chatView.chatFeedItems).hasSize(2)

        val chatItem = chatView.chatFeedItems.single { it is ChatItemView } as ChatItemView
        assertThat(chatItem.username).isEqualTo("user1")
        assertThat(chatItem.text).isEqualTo("My chat!")
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
        val result = PuzzleResult(
            id = 1L,
            userId = 1L,
            game = Game.WORLDLE,
            score = 5,
            puzzleNumber = 123,
            puzzleDate = null,
            instantSubmitted = Instant.now(),
            shareText = "",
            resultInfo = WorldleInfo(
                percentage = 100,
            ),
        )
        every { chatDAO.chatsForPuzzleDescending(Puzzle(Game.WORLDLE, 123, null)) } returns listOf(chat)
        every { resultService.allResultsForPuzzle(Puzzle(Game.WORLDLE, 123, null)) } returns listOf(result)
        every { userService.getUsernameCached(1L) } returns "user1"

        val chatView = chatService.chatView(testUser, Game.WORLDLE, 123)

        assertThat(chatView.chatFeedItems).hasSize(2)
        val items = chatView.chatFeedItems
        assertThat(items[0]).isInstanceOf(ChatItemView::class.java)
        assertThat(items[1]).isInstanceOf(ResultItemView::class.java)
    }

    @Test
    fun `when the given user has not submitted a result, chats are hidden`() {
        val chat = Chat(
            id = 1L,
            userId = 1L,
            game = Game.WORLDLE,
            puzzleNumber = 123,
            text = "My chat!",
            instantSubmitted = Instant.now(),
        )
        every { chatDAO.chatsForPuzzleDescending(Puzzle(Game.WORLDLE, 123, null)) } returns listOf(chat)
        every { resultService.allResultsForPuzzle(Puzzle(Game.WORLDLE, 123, null)) } returns emptyList()
        every { userService.getUsernameCached(1L) } returns "user1"

        val chatView = chatService.chatView(testUser, Game.WORLDLE, 123)

        assertThat(chatView.chatFeedItems).hasSize(1)
        val item = chatView.chatFeedItems.single()
        assertThat(item).isInstanceOf(HiddenChatItemView::class.java)
        val hiddenChatItem = item as HiddenChatItemView
        assertThat(hiddenChatItem.username).isEqualTo("user1")
    }

    @Test
    fun `when the given user has not submitted a result, the comment button is disabled`() {
        every { chatDAO.chatsForPuzzleDescending(Puzzle(Game.WORLDLE, 123, null)) } returns emptyList()
        every { resultService.allResultsForPuzzle(Puzzle(Game.WORLDLE, 123, null)) } returns emptyList()
        every { userService.getUsernameCached(1L) } returns "user1"

        val chatView = chatService.chatView(testUser, Game.WORLDLE, 123)

        assertThat(chatView.isCommentDisabled).isTrue()
    }

    @Test
    fun `when the given user has submitted a result, the comment button is enabled`() {
        every { chatDAO.chatsForPuzzleDescending(Puzzle(Game.WORLDLE, 123, null)) } returns emptyList()
        every { resultService.allResultsForPuzzle(Puzzle(Game.WORLDLE, 123, null)) } returns listOf(worldleResult)
        every { userService.getUsernameCached(1L) } returns "user1"

        val chatView = chatService.chatView(testUser, Game.WORLDLE, 123)

        assertThat(chatView.isCommentDisabled).isFalse()
    }

    @Test
    fun `chat view includes update time string of now in long form`() {
        val chatView = chatService.chatView(testUser, Game.WORLDLE, 123)
        val clockTimeLongForm = displayTimeService.longDisplayTime(clock.instant(), testUser.id)
        val updateTimeString = "Updated $clockTimeLongForm"

        assertThat(chatView.updateTimeString).isEqualTo(updateTimeString)
    }

    @Test
    fun `uses user's time zone`() {
        chatService.chatView(testUser, Game.WORLDLE, 123)

        verify { displayTimeService.longDisplayTime(clock.instant(), testUser.id) }
    }

    @Test
    fun `renders AnswerView when one is returned by AnswerService and user has submitted a result`() {
        val answer = "answer"
        every { answerService.answerForPuzzle(Puzzle(Game.WORLDLE, 123, null)) } returns answer
        every { resultService.allResultsForPuzzle(Puzzle(Game.WORLDLE, 123, null)) } returns listOf(
            worldleResult.copy(userId = zach.id)
        )

        val chatView = chatService.chatView(zach, Game.WORLDLE, 123)
        val chatNav = chatView.navView.insideNavItem as ChatNav

        assertThat(chatNav.answerView?.answerText).isEqualTo(answer)
    }

    @Test
    fun `hides AnswerView for other users currently`() {
        val answer = "answer"
        every { answerService.answerForPuzzle(Puzzle(Game.WORLDLE, 123, null)) } returns answer
        every { resultService.allResultsForPuzzle(Puzzle(Game.WORLDLE, 123, null)) } returns listOf(
            worldleResult.copy(userId = testUser.id)
        )

        val chatView = chatService.chatView(testUser, Game.WORLDLE, 123)
        val chatNav = chatView.navView.insideNavItem as ChatNav

        assertThat(chatNav.answerView).isNull()
    }

    @Test
    fun `hides AnswerView when user has not played`() {
        every { answerService.answerForPuzzle(Puzzle(Game.WORLDLE, 123, null)) } returns "answer"
        every { resultService.allResultsForPuzzle(Puzzle(Game.WORLDLE, 123, null)) } returns emptyList()

        val chatView = chatService.chatView(zach, Game.WORLDLE, 123)
        val chatNav = chatView.navView.insideNavItem as ChatNav

        assertThat(chatNav.answerView).isNull()
    }

    @Test
    fun `hides AnswerView when no answer is returned`() {
        every { answerService.answerForPuzzle(Puzzle(Game.WORLDLE, 123, null)) } returns null
        every { resultService.allResultsForPuzzle(Puzzle(Game.WORLDLE, 123, null)) } returns listOf(
            worldleResult.copy(userId = zach.id)
        )

        val chatView = chatService.chatView(zach, Game.WORLDLE, 123)
        val chatNav = chatView.navView.insideNavItem as ChatNav

        assertThat(chatNav.answerView).isNull()
    }

    @Test
    fun `displays HiddenAnswerView for zach when answer exists but user has not played`() {
        every { answerService.answerForPuzzle(Puzzle(Game.WORLDLE, 123, null)) } returns "answer"
        every { resultService.allResultsForPuzzle(Puzzle(Game.WORLDLE, 123, null)) } returns emptyList()

        val chatView = chatService.chatView(zach, Game.WORLDLE, 123)
        val chatNav = chatView.navView.insideNavItem as ChatNav

        assertThat(chatNav.hiddenAnswerView).isNotNull()
    }

    @Test
    fun `hides HiddenAnswerView for zach when answer does not exist`() {
        every { answerService.answerForPuzzle(Puzzle(Game.WORLDLE, 123, null)) } returns null
        every { resultService.allResultsForPuzzle(Puzzle(Game.WORLDLE, 123, null)) } returns emptyList()

        val chatView = chatService.chatView(zach, Game.WORLDLE, 123)
        val chatNav = chatView.navView.insideNavItem as ChatNav

        assertThat(chatNav.hiddenAnswerView).isNull()
    }

    @Test
    fun `hides HiddenAnswerView currently for other users even when answer exists and user has not played`() {
        every { answerService.answerForPuzzle(Puzzle(Game.WORLDLE, 123, null)) } returns "answer"
        every { resultService.allResultsForPuzzle(Puzzle(Game.WORLDLE, 123, null)) } returns emptyList()

        val chatView = chatService.chatView(testUser, Game.WORLDLE, 123)
        val chatNav = chatView.navView.insideNavItem as ChatNav

        assertThat(chatNav.hiddenAnswerView).isNull()
    }
}
