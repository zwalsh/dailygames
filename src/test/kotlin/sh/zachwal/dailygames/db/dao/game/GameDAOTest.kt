package sh.zachwal.dailygames.db.dao.game

import com.google.common.truth.Truth.assertThat
import org.jdbi.v3.core.Jdbi
import org.jdbi.v3.sqlobject.kotlin.onDemand
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import sh.zachwal.dailygames.db.extension.DatabaseExtension
import sh.zachwal.dailygames.db.jdbi.puzzle.Game
import java.time.Instant
import java.time.temporal.ChronoUnit

@ExtendWith(DatabaseExtension::class)
class GameDAOTest(
    private val jdbi: Jdbi
) {

    private val dao = jdbi.onDemand<GameDAO>()

    @Test
    fun `lists all games`() {
        val games = dao.listGames()

        assertThat(games).containsExactlyElementsIn(Game.values())
    }

    @Test
    fun `listGamesCreatedAfter returns empty for date in the future`() {
        val games = dao.listGamesCreatedAfter(
            Instant.now().plusSeconds(10)
        )

        assertThat(games).isEmpty()
    }

    @Test
    fun `listGamesCreatedAfter returns games created 4 days ago`() {
        // migration in test creates games with instant_created of 3 days ago
        val games = dao.listGamesCreatedAfter(
            Instant.now().minus(4, ChronoUnit.DAYS)
        )

        assertThat(games).containsExactlyElementsIn(Game.values())
    }

    @Test
    fun `listGamesCreatedAfter returns games just created`() {
        jdbi.withHandle<Unit, Exception> {
            it.execute(
                """
                    UPDATE game 
                    SET instant_created = now() 
                    WHERE name = 'WORLDLE';
                """.trimIndent()
            )
        }

        val games = dao.listGamesCreatedAfter(
            Instant.now().minus(1, ChronoUnit.MINUTES)
        )

        assertThat(games).containsExactly(Game.WORLDLE)
    }
}
