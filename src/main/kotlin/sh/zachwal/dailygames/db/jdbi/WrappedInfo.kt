package sh.zachwal.dailygames.db.jdbi

import org.jdbi.v3.core.mapper.reflect.ColumnName
import sh.zachwal.dailygames.db.jdbi.puzzle.Game

/**
 * TODO Will generate and store the wrapped info in the database so we don't have to repeatedly query all games.
 */
data class WrappedInfo(
    val id: Long,
    @ColumnName("user_id")
    val userId: Long,
    val totalGamesPlayed: Int,
    val gamesPlayedByGame: Map<Game, Int>,
    val pointsByGame: Map<Game, Int>,
)
