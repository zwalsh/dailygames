package sh.zachwal.dailygames.db.dao

import org.jdbi.v3.sqlobject.customizer.BindBean
import org.jdbi.v3.sqlobject.statement.SqlQuery
import org.jdbi.v3.sqlobject.statement.SqlUpdate
import sh.zachwal.dailygames.db.jdbi.puzzle.Game
import sh.zachwal.dailygames.db.jdbi.puzzle.Puzzle
import java.util.stream.Stream

interface PuzzleDAO {

    @SqlUpdate(
        """
        INSERT INTO puzzle (game, number, date) 
        VALUES (:game, :number, :date)
        """
    )
    fun insertPuzzle(@BindBean puzzle: Puzzle)

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
}
