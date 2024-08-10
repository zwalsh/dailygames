package sh.zachwal.dailygames.db

import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import java.sql.DriverManager

@ExtendWith(DatabaseExtension::class)
class TestExtension {

    @Test
    fun `can connect to db`(container: DailyGamesPostgresContainer) {
        val connection = DriverManager.getConnection(
            container.jdbcUrl,
            USERNAME,
            PASSWORD
        )
        val stmt = connection.createStatement()
        val query = "SELECT version();"
        val resultSet = stmt.executeQuery(query)
        resultSet.next()
        val result = resultSet.getString(1)
        assertThat(result).contains("12.18")
    }
}
