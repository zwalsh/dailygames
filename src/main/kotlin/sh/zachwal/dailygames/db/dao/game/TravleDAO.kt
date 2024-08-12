package sh.zachwal.dailygames.db.dao.game

import org.jdbi.v3.sqlobject.customizer.BindBean
import org.jdbi.v3.sqlobject.statement.SqlQuery
import sh.zachwal.dailygames.db.jdbi.puzzle.Puzzle
import sh.zachwal.dailygames.db.jdbi.puzzle.TravleResult
import java.util.stream.Stream

interface TravleDAO {

    @SqlQuery(
        """
            INSERT INTO travle_result
            (user_id, game, puzzle_number, instant_submitted, score, share_text, num_guesses, num_incorrect, num_perfect, num_hints)
            VALUES
            (:userId, :puzzle.game, :puzzle.number, now(), :score, :shareText, :numGuesses, :numIncorrect, :numPerfect, :numHints)
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
        numIncorrect: Int,
        numPerfect: Int,
        numHints: Int
    ): TravleResult

    @SqlQuery(
        """
            SELECT * 
            FROM travle_result
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
    ): TravleResult?

    @SqlQuery(
        """
            SELECT * 
            FROM travle_result
            WHERE puzzle_number = :puzzle.number
            ORDER BY instant_submitted DESC
        """
    )
    fun resultsForPuzzleStream(
        @BindBean("puzzle")
        puzzle: Puzzle
    ): Stream<TravleResult>

    @SqlQuery(
        """
            SELECT * 
            FROM travle_result
            ORDER BY puzzle_number DESC, instant_submitted DESC
        """
    )
    fun allResultsStream(): Stream<TravleResult>
}