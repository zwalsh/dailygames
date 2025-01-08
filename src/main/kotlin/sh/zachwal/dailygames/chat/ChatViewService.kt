package sh.zachwal.dailygames.chat

import org.jdbi.v3.core.Jdbi
import org.jdbi.v3.sqlobject.kotlin.attach
import sh.zachwal.dailygames.answers.AnswerService
import sh.zachwal.dailygames.chat.views.AnswerView
import sh.zachwal.dailygames.chat.views.ChatItemView
import sh.zachwal.dailygames.chat.views.ChatNav
import sh.zachwal.dailygames.chat.views.ChatView
import sh.zachwal.dailygames.chat.views.HiddenAnswerView
import sh.zachwal.dailygames.chat.views.HiddenChatItemView
import sh.zachwal.dailygames.chat.views.ResultItemView
import sh.zachwal.dailygames.db.dao.ChatDAO
import sh.zachwal.dailygames.db.dao.game.PuzzleDAO
import sh.zachwal.dailygames.db.jdbi.User
import sh.zachwal.dailygames.db.jdbi.puzzle.Game
import sh.zachwal.dailygames.db.jdbi.puzzle.Puzzle
import sh.zachwal.dailygames.nav.NavItem
import sh.zachwal.dailygames.nav.NavView
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
    private val answerService: AnswerService,
    private val navViewFactory: NavViewFactory,
    private val puzzleDAO: PuzzleDAO,
    private val chatDAO: ChatDAO,
    private val clock: Clock,
) {

    fun chatView(currentUser: User, game: Game, puzzleNumber: Int): ChatView {
        val puzzle = puzzleDAO.getPuzzle(game, puzzleNumber) ?: throw IllegalArgumentException("Puzzle not found")
        val results = resultService.allResultsForPuzzle(puzzle)
        val resultItems = results.map {
            ResultItemView(
                username = userService.getUsernameCached(it.userId) ?: "Unknown",
                shareText = it.shareText,
                timestampText = displayTimeService.displayTime(it.instantSubmitted, userId = currentUser.id),
                instantSubmitted = it.instantSubmitted,
            )
        }
        val hasUserSubmittedResult = results.any { it.userId == currentUser.id }
        val chats = chatDAO.chatsForPuzzleDescending(puzzle)
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

        val updateTimeString = "Updated ${displayTimeService.longDisplayTime(clock.instant(), currentUser.id)}"

        val navView = chatNav(
            username = currentUser.username,
            hasUserSubmittedResult = hasUserSubmittedResult,
            puzzle = puzzle
        )

        return ChatView(
            username = currentUser.username,
            game = game,
            puzzleNumber = puzzleNumber,
            updateTimeString = updateTimeString,
            chatFeedItems = chatFeedItems,
            isCommentDisabled = !hasUserSubmittedResult,
            navView = navView,
        )
    }

    private fun chatNav(username: String, hasUserSubmittedResult: Boolean, puzzle: Puzzle): NavView {
        val previousPuzzle = puzzleDAO.previousPuzzle(puzzle.game, puzzle.number)
        val nextPuzzle = puzzleDAO.nextPuzzle(puzzle.game, puzzle.number)

        val prevLink = previousPuzzle?.chatLink()
        val nextLink = nextPuzzle?.chatLink()

        val answerView = answerService.answerForPuzzle(puzzle)?.let { answerText ->
            AnswerView(answerText)
        }
        val chatNav = ChatNav(
            prevLink = prevLink,
            nextLink = nextLink,
            puzzle = puzzle,
            answerView = answerView?.takeIf {
                hasUserSubmittedResult
            },
            hiddenAnswerView = HiddenAnswerView.takeIf {
                answerView != null && !hasUserSubmittedResult
            },
        )
        return navViewFactory.navView(
            username = username,
            currentActiveNavItem = NavItem.CHAT,
            insideNavItem = chatNav,
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
