package sh.zachwal.dailygames.db

import liquibase.Contexts
import liquibase.Liquibase
import liquibase.database.DatabaseFactory
import liquibase.database.jvm.JdbcConnection
import liquibase.resource.DirectoryResourceAccessor
import org.junit.jupiter.api.extension.ExtensionContext
import org.junit.jupiter.api.extension.ExtensionContext.Namespace
import org.junit.jupiter.api.extension.ParameterContext
import org.junit.jupiter.api.extension.ParameterResolver
import java.io.Writer
import java.sql.DriverManager
import kotlin.io.path.Path

private val postgresContainerNamespace = Namespace.create("postgres")
private const val POSTGRES_CONTAINER_KEY = "POSTGRES_CONTAINER_KEY"

const val USERNAME = "username"
const val PASSWORD = "password"

class DatabaseExtension : ParameterResolver {

    override fun supportsParameter(parameterContext: ParameterContext, extensionContext: ExtensionContext): Boolean {
        return parameterContext.parameter.parameterizedType == DailyGamesPostgresContainer::class.java
    }

    override fun resolveParameter(parameterContext: ParameterContext, extensionContext: ExtensionContext): Any {
        return getPostgresContainer(extensionContext)
    }

    private fun getPostgresContainer(context: ExtensionContext): DailyGamesPostgresContainer {
        return context
            .getStore(postgresContainerNamespace)
            .getOrComputeIfAbsent(
                POSTGRES_CONTAINER_KEY,
                { createPostgresContainer() },
                DailyGamesPostgresContainer::class.java
            )
    }

    private fun createPostgresContainer(): DailyGamesPostgresContainer {
        val container = DailyGamesPostgresContainer()
            .withUsername(USERNAME)
            .withPassword(PASSWORD)
            .withDatabaseName("dailygames")
        container.start()
        runMigrations(container)
        return container
    }

    private fun runMigrations(container: DailyGamesPostgresContainer) {
        assert(container.isRunning)

        val database = DatabaseFactory.getInstance()
            .findCorrectDatabaseImplementation(JdbcConnection(container.jdbcConnection()))
        val liquibase = Liquibase(
            "changelog.json",
            DirectoryResourceAccessor(Path("db")),
            database
        )

        liquibase.update()
    }
}
