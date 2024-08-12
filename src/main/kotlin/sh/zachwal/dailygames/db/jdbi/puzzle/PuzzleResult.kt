package sh.zachwal.dailygames.db.jdbi.puzzle

import java.time.Instant
import java.time.LocalDate

abstract class PuzzleResult {
    abstract val id: Long
    abstract val userId: Long
    abstract val game: Game
    abstract val puzzleNumber: Int
    abstract val instantSubmitted: Instant
    abstract val puzzleDate: LocalDate?
    abstract val score: Int
    abstract val shareText: String
}
