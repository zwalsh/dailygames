package sh.zachwal.dailygames.db.jdbi

import org.jdbi.v3.core.mapper.reflect.ColumnName
import sh.zachwal.dailygames.db.jdbi.puzzle.Game
import java.time.LocalDate

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
    val bestDay: LocalDate?,
    val bestDayPoints: Int,
    val totalMinutes: Int,
    val totalMinutesRank: Int,
    val bestGame: Game?, // Can be null if the user does not qualify in any game
    val pointsByGame: Map<Game, Int>,
    val longestStreak: Int,
    val longestStreakGame: Game?,
    val ranksPerGameTotal: Map<Game, Int>,
    val averagesByGame: Map<Game, Double>,
    val ranksPerGameAverage: Map<Game, Int>,
)
