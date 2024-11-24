package sh.zachwal.dailygames.db.dao.game

import org.jdbi.v3.sqlobject.statement.SqlQuery
import sh.zachwal.dailygames.db.jdbi.puzzle.Game
import java.time.Instant

interface GameDAO {

    @SqlQuery(
        """
            SELECT name FROM game
        """
    )
    fun listGames(): List<Game>

    @SqlQuery(
        """
            SELECT name FROM game
            WHERE instant_created > :instant
        """
    )
    fun listGamesCreatedAfter(instant: Instant): List<Game>
}
