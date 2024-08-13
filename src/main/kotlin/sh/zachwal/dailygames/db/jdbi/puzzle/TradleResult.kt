package sh.zachwal.dailygames.db.jdbi.puzzle

import org.jdbi.v3.core.mapper.reflect.ColumnName
import java.time.Instant
import java.time.LocalDate

/**
 *
 * Represents a record in the tradle_result table, which stores a user's result for a Tradle puzzle.
 *
 * #Tradle #890 X/6
 * 🟩🟩⬜⬜⬜
 * 🟩🟩🟩🟩⬜
 * 🟩🟩🟩🟩🟨
 * 🟩🟩🟩🟩🟨
 * 🟩🟩🟩🟩🟨
 * 🟩🟩🟩🟩🟨
 * https://oec.world/en/games/tradle
 */

data class TradleResult constructor(
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
    // Tradle columns -- none
) : PuzzleResult()
