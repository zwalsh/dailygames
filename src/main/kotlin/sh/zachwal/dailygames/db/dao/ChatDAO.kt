package sh.zachwal.dailygames.db.dao

import org.jdbi.v3.sqlobject.customizer.BindBean
import org.jdbi.v3.sqlobject.statement.SqlQuery
import sh.zachwal.dailygames.db.jdbi.Chat
import sh.zachwal.dailygames.db.jdbi.puzzle.Puzzle
import java.time.Instant

interface ChatDAO {
    @SqlQuery(
        """
            INSERT INTO chat
            (user_id, game, puzzle_number, instant_submitted, text)
            VALUES
            (:userId, :puzzle.game, :puzzle.number, now(), :text)
            RETURNING *
        """
    )
    fun insertChat(
        userId: Long,
        @BindBean("puzzle")
        puzzle: Puzzle,
        text: String
    ): Chat

    @SqlQuery(
        """
            SELECT * 
            FROM chat
            WHERE game = :puzzle.game 
            AND puzzle_number = :puzzle.number
            ORDER BY instant_submitted DESC
        """
    )
    fun chatsForPuzzleDescending(
        @BindBean("puzzle")
        puzzle: Puzzle
    ): List<Chat>

    @SqlQuery(
        """
            SELECT * 
            FROM chat
            WHERE instant_submitted > :instant
            ORDER BY instant_submitted
        """
    )
    fun allChatsSinceInstantAscending(instant: Instant): List<Chat>
}
