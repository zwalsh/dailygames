package sh.zachwal.dailygames.db.dao.game

import sh.zachwal.dailygames.db.jdbi.puzzle.Puzzle
import sh.zachwal.dailygames.db.jdbi.puzzle.PuzzleResult
import java.util.stream.Stream

interface PuzzleResultDAO<T : PuzzleResult> {
    fun resultsForPuzzle(puzzle: Puzzle): List<T>
    fun allResultsStream(): Stream<T>
}
