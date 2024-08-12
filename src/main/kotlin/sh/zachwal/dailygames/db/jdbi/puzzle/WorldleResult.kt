package sh.zachwal.dailygames.db.jdbi.puzzle

import org.jdbi.v3.core.mapper.reflect.ColumnName
import java.time.Instant
import java.util.Date

data class WorldleResult constructor(
    // Common columns
    override val id: Long,
    @ColumnName("user_id")
    override val userId: Long,
    @ColumnName("puzzle_id")
    override val puzzleId: Long,
    @ColumnName("instant_submitted")
    override val instantSubmitted: Instant,
    @ColumnName("puzzle_date")
    override val puzzleDate: Date,
    override val score: Int,
    @ColumnName("share_text")
    override val shareText: String,

    // Worldle columns
    @ColumnName("score_percentage")
    val scorePercentage: Int
) : PuzzleResult()
