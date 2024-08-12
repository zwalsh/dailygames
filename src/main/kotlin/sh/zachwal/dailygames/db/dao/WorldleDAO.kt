package sh.zachwal.dailygames.db.dao

import org.jdbi.v3.sqlobject.customizer.BindBean
import org.jdbi.v3.sqlobject.statement.SqlQuery
import sh.zachwal.dailygames.db.jdbi.puzzle.Game
import sh.zachwal.dailygames.db.jdbi.puzzle.Puzzle
import sh.zachwal.dailygames.db.jdbi.puzzle.WorldleResult
import java.time.LocalDate

interface WorldleDAO {

    @SqlQuery(
        """
            INSERT INTO worldle_result
            (user_id, game, puzzle_number, puzzle_date, instant_submitted, score, share_text, score_percentage)
            VALUES
            (:userId, :puzzle.game, :puzzle.number, :puzzle.date, now(), :score, :shareText, :scorePercentage)
            RETURNING *
        """
    )
    fun insertWorldleResult(
        userId: Long,
        @BindBean("puzzle")
        puzzle: Puzzle,
        score: Int,
        shareText: String,
        scorePercentage: Int
    ): WorldleResult
}