package sh.zachwal.dailygames.answers

import sh.zachwal.dailygames.db.jdbi.puzzle.Game
import sh.zachwal.dailygames.db.jdbi.puzzle.Puzzle

abstract class GameAnswerService(
    private val game: Game
) {

    fun answerForPuzzle(puzzle: Puzzle): String? {
        if (puzzle.game != game) {
            throw IllegalArgumentException("Puzzle game ${puzzle.game} does not match service game $game")
        }
        return answer(puzzle)
    }

    protected abstract fun answer(puzzle: Puzzle): String?
}