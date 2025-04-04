package sh.zachwal.dailygames.config

import io.ktor.config.ApplicationConfig

data class AppConfig(
    val env: String,
    val host: String,
    val dbNameOverride: String?,
    val dbUserOverride: String?,
    val dbPasswordOverride: String?,
    val sentryConfig: SentryConfig,
    val umamiConfig: UmamiConfig,
) {
    constructor(config: ApplicationConfig) : this(
        env = config.property("ktor.deployment.environment").getString(),
        host = config.property("ktor.deployment.ws_host").getString(),
        dbNameOverride = config.propertyOrNull("ktor.deployment.db_name")?.getString(),
        dbUserOverride = config.propertyOrNull("ktor.deployment.db_user")?.getString(),
        dbPasswordOverride = config.propertyOrNull("ktor.deployment.db_password")?.getString(),
        sentryConfig = SentryConfig(
            kotlinDsn = config.property("ktor.sentry.kotlinDsn").getString(),
            jsDsn = config.property("ktor.sentry.jsDsn").getString(),
        ),
        umamiConfig = UmamiConfig(
            umamiUrl = config.property("ktor.umami.url").getString(),
            websiteId = config.property("ktor.umami.websiteId").getString(),
        )
    )
}
