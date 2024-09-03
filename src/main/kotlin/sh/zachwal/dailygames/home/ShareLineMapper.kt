package sh.zachwal.dailygames.home

import sh.zachwal.dailygames.db.jdbi.puzzle.FlagleResult
import sh.zachwal.dailygames.db.jdbi.puzzle.PuzzleResult
import sh.zachwal.dailygames.db.jdbi.puzzle.Top5Result
import sh.zachwal.dailygames.db.jdbi.puzzle.TradleResult
import sh.zachwal.dailygames.db.jdbi.puzzle.TravleResult
import sh.zachwal.dailygames.db.jdbi.puzzle.WorldleResult
import javax.inject.Singleton

@Singleton
class ShareLineMapper {

    fun mapToShareLine(puzzleResult: PuzzleResult): String {
        return when (puzzleResult) {
            is FlagleResult -> TODO()
            is Top5Result -> TODO()
            is TradleResult -> TODO()
            is TravleResult -> TODO()
            is WorldleResult -> puzzleResult.toShareLine()
        }
    }

    private fun WorldleResult.toShareLine(): String {
        if (score == 7) {
            return "${game.emoji()} Worldle #$puzzleNumber X/6 ($scorePercentage%)"
        }
        return "${game.emoji()} Worldle #$puzzleNumber $score/6"
    }
}