package sh.zachwal.dailygames.db.jdbi

import org.jdbi.v3.json.Json
import sh.zachwal.dailygames.db.jdbi.puzzle.Game
import sh.zachwal.dailygames.results.gameinfo.ResultInfo
import java.time.Instant
import java.time.LocalDate

data class Result(
    val id: Long,
    val userId: Long,
    val game: Game,
    val puzzleNumber: Int,
    val instantSubmitted: Instant,
    val puzzleDate: LocalDate?,
    val score: Int,
    val shareText: String,
    @Json
    val resultInfo: ResultInfo,
)
