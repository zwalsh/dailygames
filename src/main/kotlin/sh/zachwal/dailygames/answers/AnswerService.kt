package sh.zachwal.dailygames.answers

import javax.inject.Inject
import javax.inject.Singleton
import sh.zachwal.dailygames.db.jdbi.puzzle.Puzzle

@Singleton
class AnswerService @Inject constructor() {

    fun answerForPuzzle(puzzle: Puzzle): String? {
        // TODO currently hardcoded to Albania for UI testing
        // Should switch on the game and delegate to a specific implementation
        // May decide to query db or hit a committed file etc.
        return "Albania - \uD83C\uDDE6\uD83C\uDDF1"
    }
}