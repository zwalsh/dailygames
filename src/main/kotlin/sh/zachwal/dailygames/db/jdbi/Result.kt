package sh.zachwal.dailygames.db.jdbi

import java.time.Instant
import java.time.LocalDate

data class Result(
    val id: Long,
    val userId: Long,
    val game: String,
    val puzzleNumber: Int,
    val instantSubmitted: Instant,
    val puzzleDate: LocalDate?,
    val score: Int,
    val shareText: String
)
