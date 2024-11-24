package sh.zachwal.dailygames.db.dao.game

import org.jdbi.v3.sqlobject.statement.SqlQuery
import sh.zachwal.dailygames.db.jdbi.puzzle.Game

interface GameDAO {

    @SqlQuery(
        """
            SELECT name FROM game
        """
    )
    fun listGames(): List<Game>
}
