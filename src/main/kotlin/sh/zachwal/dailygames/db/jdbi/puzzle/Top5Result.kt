package sh.zachwal.dailygames.db.jdbi.puzzle

import org.jdbi.v3.core.mapper.reflect.ColumnName
import java.time.Instant
import java.time.LocalDate

/**
 *
 * Represents a record in the top5_result table, which stores a user's result for a Top 5 puzzle.
 *
 * Top 5 #171
 * ⬜🟧🟨⬜⬜🟩⬜⬜
 *
 */

data class Top5Result constructor(
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

    // Top5 columns
    @ColumnName("num_guesses")
    val numGuesses: Int,
    @ColumnName("num_correct")
    val numCorrect: Int,
    @ColumnName("is_perfect")
    val isPerfect: Boolean,
) : PuzzleResult()
