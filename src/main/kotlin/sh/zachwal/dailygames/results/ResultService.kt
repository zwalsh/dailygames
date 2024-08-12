package sh.zachwal.dailygames.results

import sh.zachwal.dailygames.db.dao.PuzzleDAO
import sh.zachwal.dailygames.db.dao.WorldleDAO
import sh.zachwal.dailygames.db.jdbi.User
import sh.zachwal.dailygames.db.jdbi.puzzle.Game
import sh.zachwal.dailygames.db.jdbi.puzzle.Puzzle
import sh.zachwal.dailygames.db.jdbi.puzzle.PuzzleResult
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ResultService @Inject constructor(
    private val puzzleDAO: PuzzleDAO,
    private val worldleDAO: WorldleDAO,
    private val shareTextParser: ShareTextParser,
) {

    fun createResult(
        user: User,
        shareText: String
    ): PuzzleResult {
        // regex & parse share text
        val game = shareTextParser.identifyGame(shareText) ?: run {
            throw IllegalArgumentException("Share text could not be recognized as a valid game")
        }

        when (game) {
            Game.WORLDLE -> {
                val worldleInfo = shareTextParser.extractWorldleInfo(shareText)
                val puzzle = getOrCreatePuzzle(Puzzle(Game.WORLDLE, worldleInfo.puzzleNumber, worldleInfo.date))

                return worldleDAO.insertResult(
                    userId = user.id,
                    puzzle = puzzle,
                    score = worldleInfo.score,
                    shareText = worldleInfo.shareTextNoLink,
                    scorePercentage = worldleInfo.percentage,
                )
            }
        }
    }

    private fun getOrCreatePuzzle(puzzle: Puzzle): Puzzle {
        return puzzleDAO.getPuzzle(puzzle.game, puzzle.number) ?: puzzleDAO.insertPuzzle(puzzle)
    }
}