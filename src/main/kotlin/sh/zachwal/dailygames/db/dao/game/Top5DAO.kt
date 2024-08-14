package sh.zachwal.dailygames.db.dao.game

import org.jdbi.v3.sqlobject.customizer.BindBean
import org.jdbi.v3.sqlobject.statement.SqlQuery
import sh.zachwal.dailygames.db.jdbi.puzzle.Puzzle
import sh.zachwal.dailygames.db.jdbi.puzzle.Top5Result
import java.util.stream.Stream

interface Top5DAO : PuzzleResultDAO<Top5Result> {

    @SqlQuery(
        """
            INSERT INTO top5_result
            (user_id, game, puzzle_number, instant_submitted, score, share_text, num_guesses, num_correct, is_perfect)
            VALUES
            (:userId, :puzzle.game, :puzzle.number, now(), :score, :shareText, :numGuesses, :numCorrect, :isPerfect)
            RETURNING *
        """
    )
    fun insertResult(
        userId: Long,
        @BindBean("puzzle")
        puzzle: Puzzle,
        score: Int,
        shareText: String,
        numGuesses: Int,
        numCorrect: Int,
        isPerfect: Boolean,
    ): Top5Result

    @SqlQuery(
        """
            SELECT * 
            FROM top5_result
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
    ): Top5Result?

    @SqlQuery(
        """
            SELECT * 
            FROM top5_result
            WHERE puzzle_number = :puzzle.number
            ORDER BY instant_submitted DESC
        """
    )
    fun resultsForPuzzleStream(
        @BindBean("puzzle")
        puzzle: Puzzle
    ): Stream<Top5Result>

    @SqlQuery(
        """
            SELECT * 
            FROM top5_result
            ORDER BY puzzle_number DESC, instant_submitted DESC
        """
    )
    override fun allResultsStream(): Stream<Top5Result>
}
