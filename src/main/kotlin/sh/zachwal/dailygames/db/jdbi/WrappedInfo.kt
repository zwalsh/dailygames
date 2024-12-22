package sh.zachwal.dailygames.db.jdbi

import org.jdbi.v3.core.mapper.reflect.ColumnName
import sh.zachwal.dailygames.db.jdbi.puzzle.Game

/**
 * TODO Will generate and store the wrapped info in the database so we don't have to repeatedly query all games.
 */
data class WrappedInfo constructor(
    val id: Long,
    @ColumnName("user_id")
    val userId: Long,
    val totalGamesPlayed: Int,
    val totalGamesRank: Int,
    val totalPoints: Int,
    val totalPointsRank: Int,
    val favoriteGame: Game,
    val gamesPlayedByGame: Map<Game, Int>,
    val pointsByGame: Map<Game, Int>,
    val totalMinutes: Int,
    val totalMinutesRank: Int,
    val bestGame: Game,
    val averagesByGame: Map<Game, Double>,
    val ranksPerGameTotal: Map<Game, Int>,
    val ranksPerGameAverage: Map<Game, Int>,
)
