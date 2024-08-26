package sh.zachwal.dailygames.db.dao.game

import org.jdbi.v3.sqlobject.customizer.BindBean
import org.jdbi.v3.sqlobject.statement.SqlQuery
import sh.zachwal.dailygames.db.jdbi.puzzle.Puzzle
import sh.zachwal.dailygames.db.jdbi.puzzle.WorldleResult
import java.time.LocalDate
import java.util.stream.Stream

interface WorldleDAO : PuzzleResultDAO<WorldleResult> {

    @SqlQuery(
        """
            INSERT INTO worldle_result
            (user_id, game, puzzle_number, puzzle_date, instant_submitted, score, share_text, score_percentage)
            VALUES
            (:userId, :puzzle.game, :puzzle.number, :puzzle.date, now(), :score, :shareText, :scorePercentage)
            RETURNING *
        """
    )
    fun insertResult(
        userId: Long,
        @BindBean("puzzle")
        puzzle: Puzzle,
        score: Int,
        shareText: String,
        scorePercentage: Int
    ): WorldleResult

    @SqlQuery(
        """
            SELECT * 
            FROM worldle_result
            WHERE user_id = :userId
            AND puzzle_date = :date
            ORDER BY instant_submitted DESC
            LIMIT 1
        """
    )
    fun resultForUserOnDate(
        userId: Long,
        date: LocalDate
    ): WorldleResult?

    @SqlQuery(
        """
            SELECT * 
            FROM worldle_result
            WHERE game = :puzzle.game
            AND puzzle_number = :puzzle.number
            ORDER BY instant_submitted DESC
        """
    )
    override fun resultsForPuzzle(
        @BindBean("puzzle")
        puzzle: Puzzle
    ): List<WorldleResult>

    @SqlQuery(
        """
            SELECT * 
            FROM worldle_result
            ORDER BY instant_submitted DESC
        """
    )
    override fun allResultsStream(): Stream<WorldleResult>
}
