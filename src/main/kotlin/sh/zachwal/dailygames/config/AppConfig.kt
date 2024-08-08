package sh.zachwal.dailygames.config

import io.ktor.config.ApplicationConfig

data class AppConfig(
    val env: String,
    val host: String,
    val dbNameOverride: String?,
    val dbUserOverride: String?,
    val dbPasswordOverride: String?,
//    val sentryConfig: SentryConfig
) {
    constructor(config: ApplicationConfig) : this(
        env = config.property("ktor.deployment.environment").getString(),
        host = config.property("ktor.deployment.ws_host").getString(),
        dbNameOverride = config.propertyOrNull("ktor.deployment.db_name")?.getString(),
        dbUserOverride = config.propertyOrNull("ktor.deployment.db_user")?.getString(),
        dbPasswordOverride = config.propertyOrNull("ktor.deployment.db_password")?.getString(),
    )
}
