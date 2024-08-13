package sh.zachwal.dailygames.db.jdbi.puzzle

import org.jdbi.v3.core.mapper.reflect.ColumnName
import java.time.Instant
import java.time.LocalDate

/**
 *
 * Represents a record in the travle_result table, which stores a user's result for a Travle puzzle.
 *
 * #travle #606 +2 (1 hint)
 * ✅✅🟩🟧🟧✅
 */

data class TravleResult constructor(
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

    // Travle columns
    @ColumnName("num_guesses")
    val numGuesses: Int,
    @ColumnName("num_incorrect")
    val numIncorrect: Int,
    @ColumnName("num_perfect")
    val numPerfect: Int,
    @ColumnName("num_hints")
    val numHints: Int,
) : PuzzleResult()
