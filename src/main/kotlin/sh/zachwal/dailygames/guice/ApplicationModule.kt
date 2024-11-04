package sh.zachwal.dailygames.guice

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.google.inject.AbstractModule
import com.google.inject.Provides
import com.google.inject.Singleton
import com.google.inject.name.Named
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.asCoroutineDispatcher
import sh.zachwal.dailygames.leaderboard.MINIMUM_GAMES_FOR_AVERAGE
import java.time.Clock
import java.util.concurrent.Executors

class ApplicationModule : AbstractModule() {

    override fun configure() {
        bind(Clock::class.java).toInstance(Clock.systemUTC())
        bind(ObjectMapper::class.java).toInstance(jacksonObjectMapper())
    }

    @Provides
    @Singleton
    @Named("presserDispatcher")
    fun presserDispatcher(): CoroutineDispatcher = Executors.newFixedThreadPool(4)
        .asCoroutineDispatcher()

    @Provides
    @Named("leaderboardMinimumGamesForAverage")
    fun minimumGamesForAverage(): Int = MINIMUM_GAMES_FOR_AVERAGE
}
