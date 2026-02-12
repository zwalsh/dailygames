package sh.zachwal.dailygames.guice

import com.google.inject.AbstractModule
import com.google.inject.Provides
import com.google.inject.name.Named
import io.ktor.server.config.ApplicationConfig
import sh.zachwal.dailygames.config.AppConfig

class ConfigModule(
    private val applicationConfig: ApplicationConfig,
) : AbstractModule() {

    override fun configure() {
        bind(AppConfig::class.java).toInstance(AppConfig(applicationConfig))
    }

    @Provides
    @Named("host")
    fun hostString(appConfig: AppConfig) = appConfig.host
}
