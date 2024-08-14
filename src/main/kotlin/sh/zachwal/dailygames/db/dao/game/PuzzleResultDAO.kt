package sh.zachwal.dailygames.db.dao.game

import sh.zachwal.dailygames.db.jdbi.puzzle.PuzzleResult
import java.util.stream.Stream

interface PuzzleResultDAO<T : PuzzleResult> {
    fun allResultsStream(): Stream<T>
}