package sh.zachwal.dailygames.db.dao.game

import org.jdbi.v3.sqlobject.customizer.BindBean
import org.jdbi.v3.sqlobject.statement.SqlQuery
import sh.zachwal.dailygames.db.jdbi.puzzle.GeocirclesResult
import sh.zachwal.dailygames.db.jdbi.puzzle.Puzzle
import java.time.Instant
import java.util.stream.Stream

interface GeocirclesDAO : PuzzleResultDAO<GeocirclesResult> {

    @SqlQuery(
        """
            INSERT INTO geocircles_result
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
    ): GeocirclesResult

    @SqlQuery(
        """
            SELECT * 
            FROM geocircles_result
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
    ): GeocirclesResult?

    @SqlQuery(
        """
            SELECT * 
            FROM geocircles_result
            WHERE game = :puzzle.game
            AND puzzle_number = :puzzle.number
            ORDER BY instant_submitted DESC
        """
    )
    override fun resultsForPuzzle(
        @BindBean("puzzle")
        puzzle: Puzzle
    ): List<GeocirclesResult>

    @SqlQuery(
        """
            SELECT * 
            FROM geocircles_result
            ORDER BY instant_submitted DESC
        """
    )
    override fun allResultsStream(): Stream<GeocirclesResult>

    @SqlQuery(
        """
            SELECT * 
            FROM geocircles_result
            WHERE user_id = :userId
            AND instant_submitted >= :start
            AND instant_submitted <= :end
        """
    )
    override fun resultsForUserInTimeRange(userId: Long, start: Instant, end: Instant): List<GeocirclesResult>
}
