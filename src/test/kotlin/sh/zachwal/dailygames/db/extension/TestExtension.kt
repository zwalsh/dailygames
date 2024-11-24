package sh.zachwal.dailygames.db.extension

import com.google.common.truth.Truth.assertThat
import org.jdbi.v3.core.Jdbi
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import java.time.Instant
import java.time.temporal.ChronoUnit

@ExtendWith(DatabaseExtension::class)
class TestExtension {

    @Test
    fun `can connect to db`(container: DailyGamesPostgresContainer) {
        val stmt = container.jdbcConnection().createStatement()
        val query = "SELECT version();"
        val resultSet = stmt.executeQuery(query)
        resultSet.next()
        val result = resultSet.getString(1)
        assertThat(result).contains("12.18")
    }

    @Test
    fun `runs migrations`(container: DailyGamesPostgresContainer) {
        val stmt = container.jdbcConnection().createStatement()
        val query = "SELECT table_name FROM information_schema.tables WHERE table_schema = 'public';"
        val resultSet = stmt.executeQuery(query)
        val tables = mutableListOf<String>()
        while (resultSet.next()) {
            tables.add(resultSet.getString(1))
        }
        assertThat(tables).containsExactly(
            "databasechangelog",
            "databasechangeloglock",
            "user",
            "session",
            "role",
            "game",
            "puzzle",
            "chat",
            "user_preferences",
            "result",
        )
    }

    @Test
    fun `creates Jdbi instance`(jdbi: Jdbi) {
        val version = jdbi.withHandle<String, Exception> { handle ->
            handle.createQuery("SELECT version();")
                .mapTo(String::class.java)
                .single()
        }
        assertThat(version).contains("12.18")
    }
}
