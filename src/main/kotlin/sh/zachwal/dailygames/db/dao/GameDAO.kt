package sh.zachwal.dailygames.db.dao

import org.jdbi.v3.sqlobject.statement.SqlQuery
import sh.zachwal.dailygames.db.jdbi.puzzle.Game

interface GameDAO {

    @SqlQuery(
        """
            SELECT * FROM game
        """
    )
    fun listGames(): List<Game>
}
