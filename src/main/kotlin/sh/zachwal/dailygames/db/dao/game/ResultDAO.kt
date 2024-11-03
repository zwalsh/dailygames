package sh.zachwal.dailygames.db.dao.game

import org.jdbi.v3.json.Json
import org.jdbi.v3.sqlobject.customizer.BindBean
import org.jdbi.v3.sqlobject.statement.SqlQuery
import sh.zachwal.dailygames.db.jdbi.Result
import sh.zachwal.dailygames.db.jdbi.puzzle.Puzzle
import sh.zachwal.dailygames.results.resultinfo.ResultInfo

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
}