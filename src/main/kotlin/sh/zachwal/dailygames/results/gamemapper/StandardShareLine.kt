package sh.zachwal.dailygames.results.gamemapper

import sh.zachwal.dailygames.db.jdbi.puzzle.PuzzleResult
import sh.zachwal.dailygames.leaderboard.PointCalculator

fun PuzzleResult.toStandardShareLine(pointCalculator: PointCalculator): String {
    val gameAndPuzzle = "${game.emoji()} ${game.displayName()} #$puzzleNumber"
    val maxPoints = pointCalculator.maxPoints(this)
    if (score == maxPoints + 1) {
        return "$gameAndPuzzle X/$maxPoints"
    }
    val line = "$gameAndPuzzle $score/$maxPoints"
    return if (score == 1) {
        "$line ${game.perfectEmoji()}"
    } else {
        line
    }
}
