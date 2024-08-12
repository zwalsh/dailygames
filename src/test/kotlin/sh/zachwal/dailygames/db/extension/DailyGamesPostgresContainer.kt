package sh.zachwal.dailygames.db.extension

import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.utility.DockerImageName
import java.sql.Connection
import java.sql.DriverManager

class DailyGamesPostgresContainer :
    PostgreSQLContainer<DailyGamesPostgresContainer>(DockerImageName.parse("postgres:12.18")) {
    fun jdbcConnection(): Connection {
        return DriverManager.getConnection(
            jdbcUrl,
            USERNAME,
            PASSWORD
        )
    }
}
