package sh.zachwal.dailygames.db.jdbi

import org.jdbi.v3.core.mapper.reflect.ColumnName
import sh.zachwal.dailygames.db.jdbi.puzzle.Game
import java.time.Instant

data class Chat(
    val id: Long,
    @ColumnName("user_id")
    val userId: Long,
    val game: Game,
    @ColumnName("puzzle_number")
    val puzzleNumber: Int,
    @ColumnName("instant_submitted")
    val instantSubmitted: Instant,
    val text: String,
)
