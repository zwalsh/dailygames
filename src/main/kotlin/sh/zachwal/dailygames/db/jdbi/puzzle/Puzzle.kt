package sh.zachwal.dailygames.db.jdbi.puzzle

import java.util.Date

data class Puzzle(
    val game: Game,
    val number: Int,
    val date: Date?,
)
