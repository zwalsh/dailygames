package sh.zachwal.dailygames.db.jdbi.puzzle

import java.time.LocalDate

data class Puzzle(
    val game: Game,
    val number: Int,
    val date: LocalDate?,
)
