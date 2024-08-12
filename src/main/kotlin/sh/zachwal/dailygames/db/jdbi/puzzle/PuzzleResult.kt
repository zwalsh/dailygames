package sh.zachwal.dailygames.db.jdbi.puzzle

import java.time.Instant
import java.util.Date

abstract class PuzzleResult {
    abstract val id: Long
    abstract val userId: Long
    abstract val game: Game
    abstract val puzzleNumber: Int
    abstract val instantSubmitted: Instant
    abstract val puzzleDate: Date
    abstract val score: Int
    abstract val shareText: String
}
