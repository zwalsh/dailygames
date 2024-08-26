package sh.zachwal.dailygames.chat

import org.jdbi.v3.core.Jdbi
import org.jdbi.v3.sqlobject.kotlin.attach
import sh.zachwal.dailygames.chat.views.ChatFeedItemView
import sh.zachwal.dailygames.chat.views.ChatView
import sh.zachwal.dailygames.db.dao.game.PuzzleDAO
import sh.zachwal.dailygames.db.jdbi.puzzle.Game
import sh.zachwal.dailygames.db.jdbi.puzzle.Puzzle
import sh.zachwal.dailygames.results.ResultService
import sh.zachwal.dailygames.results.displayTime
import sh.zachwal.dailygames.users.UserService
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ChatService @Inject constructor(
    private val jdbi: Jdbi,
    private val resultService: ResultService,
    private val userService: UserService,
) {

    fun chatView(username: String, game: Game, puzzleNumber: Int): ChatView {
        val results = resultService.allResultsForPuzzle(Puzzle(game, puzzleNumber, date = null))
        val chatFeedItems = results.reversed().map {
            ChatFeedItemView(
                username = userService.getUsernameCached(it.userId) ?: "Unknown",
                shareText = it.shareText,
                timestampText = displayTime(it.instantSubmitted)
            )
        }

        return ChatView(
            username = username,
            game = game,
            puzzleNumber = puzzleNumber,
            chatFeedItems = chatFeedItems
        )
    }

    fun chatViewLatest(username: String, game: Game): ChatView {
        val latestPuzzleNumber = jdbi.open().use { handle ->
            val puzzleDAO = handle.attach<PuzzleDAO>()
            puzzleDAO.listPuzzlesForGameDescending(game)
                .findFirst()
                .map { it.number }
                .orElse(1)
        }

        return chatView(username, game, latestPuzzleNumber)
    }
}