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
            SELECT * 
            FROM result
            WHERE puzzle_number = :puzzle.number
            AND game = :puzzle.game
        """
    )
    fun resultsForPuzzle(puzzle: Puzzle): List<Result>

//    fun allResultsStream(): Stream<Result>
//
//    fun allResultsForGameStream(game: Game): Stream<Result>
//
//    fun resultsForUserInTimeRange(userId: Long, start: Instant, end: Instant): List<Result>
}
