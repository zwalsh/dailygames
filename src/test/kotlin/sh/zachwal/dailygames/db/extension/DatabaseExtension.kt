package sh.zachwal.dailygames.db.extension

import liquibase.Liquibase
import liquibase.database.DatabaseFactory
import liquibase.database.jvm.JdbcConnection
import liquibase.resource.DirectoryResourceAccessor
import org.jdbi.v3.core.Jdbi
import org.jdbi.v3.core.kotlin.KotlinPlugin
import org.jdbi.v3.postgres.PostgresPlugin
import org.jdbi.v3.sqlobject.kotlin.KotlinSqlObjectPlugin
import org.junit.jupiter.api.extension.AfterEachCallback
import org.junit.jupiter.api.extension.BeforeEachCallback
import org.junit.jupiter.api.extension.ExtensionContext
import org.junit.jupiter.api.extension.ExtensionContext.Namespace
import org.junit.jupiter.api.extension.ParameterContext
import org.junit.jupiter.api.extension.ParameterResolver
import kotlin.io.path.Path

private val postgresContainerNamespace = Namespace.create("postgres")
private const val POSTGRES_CONTAINER_KEY = "POSTGRES_CONTAINER_KEY"
private const val JDBI_KEY = "JDBI"
private const val FIXTURES_KEY = "FIXTURES"

const val USERNAME = "username"
const val PASSWORD = "password"

class DatabaseExtension : ParameterResolver, BeforeEachCallback, AfterEachCallback {

    override fun supportsParameter(parameterContext: ParameterContext, extensionContext: ExtensionContext): Boolean {
        return parameterContext.parameter.parameterizedType in listOf(
            DailyGamesPostgresContainer::class.java,
            Jdbi::class.java,
            Fixtures::class.java,
        )
    }

    override fun resolveParameter(parameterContext: ParameterContext, extensionContext: ExtensionContext): Any {
        return when (parameterContext.parameter.parameterizedType) {
            DailyGamesPostgresContainer::class.java -> getPostgresContainer(extensionContext)
            Jdbi::class.java -> getJdbiInstance(extensionContext)
            Fixtures::class.java -> getFixturesInstance(extensionContext)
            else -> throw IllegalArgumentException("Cannot resolve parameter of type ${parameterContext.parameter.parameterizedType}")
        }
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
        resetDatabase(container)
        return container
    }

    private fun resetDatabase(container: DailyGamesPostgresContainer) {
        assert(container.isRunning)

        val database = DatabaseFactory.getInstance()
            .findCorrectDatabaseImplementation(JdbcConnection(container.jdbcConnection()))
        val liquibase = Liquibase(
            "changelog.json",
            DirectoryResourceAccessor(Path("db")),
            database
        )

        liquibase.dropAll()
        liquibase.update()
    }

    private fun getJdbiInstance(context: ExtensionContext): Jdbi {
        return context
            .getStore(postgresContainerNamespace)
            .getOrComputeIfAbsent(
                JDBI_KEY,
                { createJdbiInstance(context) },
                Jdbi::class.java
            )
    }

    private fun createJdbiInstance(context: ExtensionContext): Jdbi {
        val container = getPostgresContainer(context)
        return Jdbi.create(container.jdbcConnection()).installPlugin(KotlinPlugin())
            .installPlugin(PostgresPlugin())
            .installPlugin(KotlinSqlObjectPlugin())
    }

    private fun getFixturesInstance(context: ExtensionContext): Fixtures {
        return context
            .getStore(postgresContainerNamespace)
            .getOrComputeIfAbsent(
                FIXTURES_KEY,
                { createFixturesInstance(context) },
                Fixtures::class.java
            )
    }

    private fun createFixturesInstance(context: ExtensionContext): Fixtures {
        val fixtures = Fixtures(getJdbiInstance(context))
        return fixtures
    }

    override fun afterEach(context: ExtensionContext) {
        val container = getPostgresContainer(context)
        resetDatabase(container)
    }

    override fun beforeEach(context: ExtensionContext) {
        val fixtures = getFixturesInstance(context)
        fixtures.runFixtures()
    }
}
