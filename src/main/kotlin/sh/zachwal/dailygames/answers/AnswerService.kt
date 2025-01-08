package sh.zachwal.dailygames.answers

import sh.zachwal.dailygames.db.jdbi.puzzle.Game
import sh.zachwal.dailygames.db.jdbi.puzzle.Puzzle
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AnswerService @Inject constructor(
    private val worldleAnswerService: WorldleAnswerService,
    private val tradleAnswerService: TradleAnswerService,
    private val flagleAnswerService: FlagleAnswerService,
) {

    fun answerForPuzzle(puzzle: Puzzle): String? {
        return when (puzzle.game) {
            Game.WORLDLE -> worldleAnswerService.answerForPuzzle(puzzle)
            Game.TRADLE -> tradleAnswerService.answerForPuzzle(puzzle)
            Game.FLAGLE -> flagleAnswerService.answerForPuzzle(puzzle)
            else -> null
        }
    }
}
