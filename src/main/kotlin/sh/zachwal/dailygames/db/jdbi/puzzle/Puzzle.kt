package sh.zachwal.dailygames.db.jdbi.puzzle

import java.util.Date

data class Puzzle(
    val id: Long,
    val game: Game,
    val date: Date,
)
