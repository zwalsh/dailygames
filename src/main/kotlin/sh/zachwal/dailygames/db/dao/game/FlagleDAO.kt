package sh.zachwal.dailygames.db.dao.game

import org.jdbi.v3.sqlobject.customizer.BindBean
import org.jdbi.v3.sqlobject.statement.SqlQuery
import sh.zachwal.dailygames.db.jdbi.puzzle.FlagleResult
import sh.zachwal.dailygames.db.jdbi.puzzle.Puzzle
import java.time.LocalDate
import java.util.stream.Stream

interface FlagleDAO : PuzzleResultDAO<FlagleResult> {

    @SqlQuery(
        """
            INSERT INTO flagle_result
            (user_id, game, puzzle_number, puzzle_date, instant_submitted, score, share_text)
            VALUES
            (:userId, :puzzle.game, :puzzle.number, :puzzle.date, now(), :score, :shareText)
            RETURNING *
        """
    )
    fun insertResult(
        userId: Long,
        @BindBean("puzzle")
        puzzle: Puzzle,
        score: Int,
        shareText: String
    ): FlagleResult

    @SqlQuery(
        """
            SELECT * 
            FROM flagle_result
            WHERE user_id = :userId
            AND puzzle_date = :date
            ORDER BY instant_submitted DESC
            LIMIT 1
        """
    )
    fun resultForUserOnDate(
        userId: Long,
        date: LocalDate
    ): FlagleResult?

    @SqlQuery(
        """
            SELECT * 
            FROM flagle_result
            WHERE game = :puzzle.game
            AND puzzle_number = :puzzle.number
            ORDER BY instant_submitted DESC
        """
    )
    override fun resultsForPuzzle(
        @BindBean("puzzle")
        puzzle: Puzzle
    ): List<FlagleResult>

    @SqlQuery(
        """
            SELECT * 
            FROM flagle_result
            ORDER BY instant_submitted DESC
        """
    )
    override fun allResultsStream(): Stream<FlagleResult>
}
