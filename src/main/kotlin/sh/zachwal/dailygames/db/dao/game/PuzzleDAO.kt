package sh.zachwal.dailygames.db.dao.game

import org.jdbi.v3.sqlobject.customizer.BindBean
import org.jdbi.v3.sqlobject.statement.SqlQuery
import sh.zachwal.dailygames.db.jdbi.puzzle.Game
import sh.zachwal.dailygames.db.jdbi.puzzle.Puzzle
import java.util.stream.Stream

interface PuzzleDAO {

    @SqlQuery(
        """
        INSERT INTO puzzle (game, number, date) 
        VALUES (:game, :number, :date)
        RETURNING *
        """
    )
    fun insertPuzzle(@BindBean puzzle: Puzzle): Puzzle

    @SqlQuery(
        """
            SELECT game, number, date
            FROM puzzle
            WHERE game = :game AND number = :number
        """
    )
    fun getPuzzle(game: Game, number: Int): Puzzle?

    @SqlQuery(
        """
            SELECT game, number, date
            FROM puzzle
            WHERE game = :game
            ORDER BY number DESC
        """
    )
    fun listPuzzlesForGameDescending(game: Game): Stream<Puzzle>

    @SqlQuery(
        """
            SELECT EXISTS(
                SELECT 1
                FROM puzzle
                WHERE game = :game AND number = :number
            )
        """
    )
    fun puzzleExists(game: Game, number: Int): Boolean
}
