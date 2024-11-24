package sh.zachwal.dailygames.results

import sh.zachwal.dailygames.db.jdbi.puzzle.Puzzle

class ConflictingPuzzleResultException(
    val puzzle: Puzzle,
    val userId: Long,
) : IllegalArgumentException("You have already submitted a result for puzzle ${puzzle.game.displayName()} #${puzzle.number}")
