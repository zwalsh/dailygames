package sh.zachwal.dailygames.chat

import org.jdbi.v3.core.Jdbi
import org.jdbi.v3.sqlobject.kotlin.attach
import sh.zachwal.dailygames.chat.views.ChatItemView
import sh.zachwal.dailygames.chat.views.ChatView
import sh.zachwal.dailygames.chat.views.HiddenChatItemView
import sh.zachwal.dailygames.chat.views.ResultItemView
import sh.zachwal.dailygames.db.dao.ChatDAO
import sh.zachwal.dailygames.db.dao.game.PuzzleDAO
import sh.zachwal.dailygames.db.jdbi.User
import sh.zachwal.dailygames.db.jdbi.puzzle.Game
import sh.zachwal.dailygames.db.jdbi.puzzle.Puzzle
import sh.zachwal.dailygames.nav.NavViewFactory
import sh.zachwal.dailygames.results.ResultService
import sh.zachwal.dailygames.users.UserService
import sh.zachwal.dailygames.utils.DisplayTimeService
import java.time.Clock
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ChatViewService @Inject constructor(
    private val jdbi: Jdbi,
    private val resultService: ResultService,
    private val userService: UserService,
    private val displayTimeService: DisplayTimeService,
    private val navViewFactory: NavViewFactory,
    private val puzzleDAO: PuzzleDAO,
    private val chatDAO: ChatDAO,
    private val clock: Clock,
) {

    fun chatView(currentUser: User, game: Game, puzzleNumber: Int): ChatView {
        val results = resultService.allResultsForPuzzle(Puzzle(game, puzzleNumber, date = null))
        val resultItems = results.map {
            ResultItemView(
                username = userService.getUsernameCached(it.userId) ?: "Unknown",
                shareText = it.shareText,
                timestampText = displayTimeService.displayTime(it.instantSubmitted, userId = currentUser.id),
                instantSubmitted = it.instantSubmitted,
            )
        }
        val hasUserSubmittedResult = results.any { it.userId == currentUser.id }
        val chats = chatDAO.chatsForPuzzleDescending(Puzzle(game, puzzleNumber, date = null))
        val chatItems = chats.map {
            if (hasUserSubmittedResult) {
                ChatItemView(
                    username = userService.getUsernameCached(it.userId) ?: "Unknown",
                    text = it.text,
                    timestampText = displayTimeService.displayTime(it.instantSubmitted, userId = currentUser.id),
                    instantSubmitted = it.instantSubmitted,
                )
            } else {
                HiddenChatItemView(
                    username = userService.getUsernameCached(it.userId) ?: "Unknown",
                    timestampText = displayTimeService.displayTime(it.instantSubmitted, userId = currentUser.id),
                    instantSubmitted = it.instantSubmitted,
                )
            }
        }

        val chatFeedItems = (resultItems + chatItems).sortedBy { it.instantSubmitted }

        val previousPuzzle = puzzleDAO.previousPuzzle(game, puzzleNumber)
        val nextPuzzle = puzzleDAO.nextPuzzle(game, puzzleNumber)

        val prevLink = previousPuzzle?.chatLink()
        val nextLink = nextPuzzle?.chatLink()

        val updateTimeString = "Updated ${displayTimeService.longDisplayTime(clock.instant(), currentUser.id)}"

        return ChatView(
            username = currentUser.username,
            game = game,
            puzzleNumber = puzzleNumber,
            updateTimeString = updateTimeString,
            chatFeedItems = chatFeedItems,
            prevLink = prevLink,
            nextLink = nextLink,
            isCommentDisabled = !hasUserSubmittedResult,
            navViewFactory = navViewFactory,
        )
    }

    fun chatViewLatest(currentUser: User, game: Game): ChatView {
        val latestPuzzleNumber = jdbi.open().use { handle ->
            val puzzleDAO = handle.attach<PuzzleDAO>()
            puzzleDAO.listPuzzlesForGameDescending(game)
                .findFirst()
                .map { it.number }
                .orElse(1)
        }

        return chatView(currentUser, game, latestPuzzleNumber)
    }
}

fun Puzzle.chatLink(): String {
    return chatLink(game, number)
}

fun chatLink(game: Game, puzzleNumber: Int): String {
    return "/game/${game.name.lowercase()}/puzzle/$puzzleNumber"
}
