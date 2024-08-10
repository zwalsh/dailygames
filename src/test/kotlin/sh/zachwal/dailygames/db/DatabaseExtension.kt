package sh.zachwal.dailygames.db

import org.junit.jupiter.api.extension.ExtensionContext
import org.junit.jupiter.api.extension.ExtensionContext.Namespace
import org.junit.jupiter.api.extension.ParameterContext
import org.junit.jupiter.api.extension.ParameterResolver

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
        return container
    }
}