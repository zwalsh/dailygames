package sh.zachwal.dailygames.db.dao.game

import org.jdbi.v3.sqlobject.customizer.BindBean
import org.jdbi.v3.sqlobject.statement.SqlQuery
import sh.zachwal.dailygames.db.jdbi.puzzle.Puzzle
import sh.zachwal.dailygames.db.jdbi.puzzle.TradleResult
import java.util.stream.Stream

interface TradleDAO {

    @SqlQuery(
        """
            INSERT INTO tradle_result
            (user_id, game, puzzle_number, instant_submitted, score, share_text)
            VALUES
            (:userId, :puzzle.game, :puzzle.number, now(), :score, :shareText)
            RETURNING *
        """
    )
    fun insertResult(
        userId: Long,
        @BindBean("puzzle")
        puzzle: Puzzle,
        score: Int,
        shareText: String
    ): TradleResult

    @SqlQuery(
        """
            SELECT * 
            FROM tradle_result
            WHERE user_id = :userId
            AND puzzle_number = :puzzle.number
            ORDER BY instant_submitted DESC
            LIMIT 1
        """
    )
    fun resultForUserOnPuzzle(
        userId: Long,
        @BindBean("puzzle")
        puzzle: Puzzle
    ): TradleResult?

    @SqlQuery(
        """
            SELECT * 
            FROM tradle_result
            WHERE puzzle_number = :puzzle.number
            ORDER BY instant_submitted DESC
        """
    )
    fun resultsForPuzzleStream(
        @BindBean("puzzle")
        puzzle: Puzzle
    ): Stream<TradleResult>

    @SqlQuery(
        """
            SELECT * 
            FROM tradle_result
            ORDER BY puzzle_number DESC, instant_submitted DESC
        """
    )
    fun allResultsStream(): Stream<TradleResult>
}