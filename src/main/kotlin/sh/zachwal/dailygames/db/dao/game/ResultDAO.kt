package sh.zachwal.dailygames.db.dao.game

import org.jdbi.v3.json.Json
import org.jdbi.v3.sqlobject.customizer.BindBean
import org.jdbi.v3.sqlobject.statement.SqlQuery
import sh.zachwal.dailygames.db.jdbi.Result
import sh.zachwal.dailygames.db.jdbi.puzzle.Game
import sh.zachwal.dailygames.db.jdbi.puzzle.Puzzle
import sh.zachwal.dailygames.results.resultinfo.ResultInfo
import java.time.Instant
import java.util.stream.Stream

interface ResultDAO {

    @SqlQuery(
        """
            INSERT INTO result
            (user_id, game, puzzle_number, instant_submitted, score, share_text, result_info)
            VALUES
            (:userId, :puzzle.game, :puzzle.number, now(), :score, :shareText, :resultInfo)
            RETURNING *
        """
    )
    fun insertResult(
        userId: Long,
        @BindBean("puzzle")
        puzzle: Puzzle,
        score: Int,
        shareText: String,
        @Json
        resultInfo: ResultInfo,
    ): Result

    @SqlQuery(
        """
            INSERT INTO result
            (user_id, game, puzzle_number, instant_submitted, score, share_text, result_info)
            VALUES
            (:userId, :puzzle.game, :puzzle.number, :instantSubmitted, :score, :shareText, :resultInfo)
            RETURNING *
        """
    )
    fun insertResultWithInstantSubmitted(
        userId: Long,
        @BindBean("puzzle")
        puzzle: Puzzle,
        score: Int,
        shareText: String,
        @Json
        resultInfo: ResultInfo,
        instantSubmitted: Instant,
    ): Result


    @SqlQuery(
        """
            SELECT * 
            FROM result
            WHERE puzzle_number = :puzzle.number
            AND game = :puzzle.game
        """
    )
    fun resultsForPuzzle(puzzle: Puzzle): List<Result>

    @SqlQuery(
        """
            SELECT * 
            FROM result
            ORDER BY instant_submitted DESC
        """
    )
    fun allResultsStream(): Stream<Result>

    @SqlQuery(
        """
            SELECT * 
            FROM result
            WHERE game = :game
            ORDER BY instant_submitted DESC
        """
    )
    fun allResultsForGameStream(game: Game): Stream<Result>

    @SqlQuery(
        """
            SELECT * 
            FROM result
            WHERE user_id = :userId
            AND instant_submitted >= :start 
            AND instant_submitted < :end
        """
    )
    fun resultsForUserInTimeRange(userId: Long, start: Instant, end: Instant): List<Result>

    @SqlQuery(
        """
            SELECT * 
            FROM result
            WHERE user_id = :userId
            AND game = :puzzle.game
            AND puzzle_number = :puzzle.number
            LIMIT 1
        """
    )
    fun findResult(userId: Long, puzzle: Puzzle): Result?
}
