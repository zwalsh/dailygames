package sh.zachwal.dailygames.db.dao.game

import com.google.common.truth.Truth.assertThat
import org.jdbi.v3.core.Jdbi
import org.jdbi.v3.sqlobject.kotlin.onDemand
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import sh.zachwal.dailygames.db.extension.DatabaseExtension
import sh.zachwal.dailygames.db.jdbi.puzzle.Game

@ExtendWith(DatabaseExtension::class)
class GameDAOTest(jdbi: Jdbi) {

    private val gameDAO: GameDAO = jdbi.onDemand()

    @Test
    fun `game table includes all games`() {
        val games = gameDAO.listGames()

        assertThat(games).containsExactly(Game.values().map { it.name })
    }
}
