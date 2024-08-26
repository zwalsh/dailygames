package sh.zachwal.dailygames.chat

import sh.zachwal.dailygames.chat.views.ChatView
import sh.zachwal.dailygames.db.jdbi.puzzle.Game
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ChatService @Inject constructor() {

    fun chatView(username: String, game: Game, puzzleNumber: Int): ChatView {
        return ChatView(username = username, game = game, puzzleNumber = puzzleNumber)
    }

    fun chatViewLatest(username: String, game: Game): ChatView {
        return ChatView(username = username, game = game, puzzleNumber = 1)
    }
}