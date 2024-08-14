package sh.zachwal.dailygames.db.jdbi.puzzle

import org.jdbi.v3.core.mapper.reflect.ColumnName
import java.time.Instant
import java.time.LocalDate

data class FlagleResult constructor(
    // Common columns
    override val id: Long,
    @ColumnName("user_id")
    override val userId: Long,
    override val game: Game,
    @ColumnName("puzzle_number")
    override val puzzleNumber: Int,
    @ColumnName("puzzle_date")
    override val puzzleDate: LocalDate?,
    @ColumnName("instant_submitted")
    override val instantSubmitted: Instant,
    override val score: Int,
    @ColumnName("share_text")
    override val shareText: String,
) : PuzzleResult()
