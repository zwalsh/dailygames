package sh.zachwal.dailygames.chat

import sh.zachwal.dailygames.chat.api.ChatResponse
import sh.zachwal.dailygames.db.dao.ChatDAO
import sh.zachwal.dailygames.db.dao.game.PuzzleDAO
import sh.zachwal.dailygames.db.jdbi.User
import sh.zachwal.dailygames.db.jdbi.puzzle.Game
import sh.zachwal.dailygames.db.jdbi.puzzle.Puzzle
import sh.zachwal.dailygames.utils.DisplayTimeService
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ChatService @Inject constructor(
    private val displayTimeService: DisplayTimeService,
    private val puzzleDAO: PuzzleDAO,
    private val chatDAO: ChatDAO,
) {
    fun insertChat(user: User, game: Game, puzzleNumber: Int, text: String): ChatResponse {
        val puzzle = Puzzle(game, puzzleNumber, date = null)
        val chat = chatDAO.insertChat(user.id, puzzle, text)
        return ChatResponse(
            username = user.username,
            displayTime = displayTimeService.displayTime(chat.instantSubmitted, user.id),
            text = chat.text,
        )
    }

    fun currentChatCounts(): Map<Game, Int> {
        val latestPuzzles = puzzleDAO.latestPuzzlePerGame()
        return latestPuzzles.associate { puzzle ->
            puzzle.game to chatDAO.chatCountForPuzzle(puzzle)
        }
    }
}
