package sh.zachwal.dailygames.chat

import org.jdbi.v3.core.Jdbi
import org.jdbi.v3.sqlobject.kotlin.attach
import sh.zachwal.dailygames.chat.views.ChatView
import sh.zachwal.dailygames.db.dao.game.PuzzleDAO
import sh.zachwal.dailygames.db.jdbi.puzzle.Game
import sh.zachwal.dailygames.results.ResultService
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ChatService @Inject constructor(
    private val jdbi: Jdbi,
    private val resultService: ResultService
) {

    fun chatView(username: String, game: Game, puzzleNumber: Int): ChatView {
        return ChatView(
            username = username,
            game = game,
            puzzleNumber = puzzleNumber,
            chatFeedItems = emptyList()
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