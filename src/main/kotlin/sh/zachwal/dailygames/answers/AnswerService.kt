package sh.zachwal.dailygames.answers

import sh.zachwal.dailygames.db.jdbi.puzzle.Game
import sh.zachwal.dailygames.db.jdbi.puzzle.Puzzle
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AnswerService @Inject constructor(
    private val tradleAnswerService: TradleAnswerService,
) {

    fun answerForPuzzle(puzzle: Puzzle): String? {
        return when (puzzle.game) {
            Game.TRADLE -> tradleAnswerService.answerForPuzzle(puzzle)
            else -> null
        }
    }
}
