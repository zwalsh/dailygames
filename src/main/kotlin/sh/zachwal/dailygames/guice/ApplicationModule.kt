package sh.zachwal.dailygames.guice

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.google.inject.AbstractModule
import com.google.inject.Provides
import com.google.inject.Singleton
import com.google.inject.multibindings.Multibinder
import com.google.inject.name.Named
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.asCoroutineDispatcher
import sh.zachwal.dailygames.leaderboard.MINIMUM_GAMES_FOR_AVERAGE
import sh.zachwal.dailygames.results.gamemapper.BandleMapper
import sh.zachwal.dailygames.results.gamemapper.BracketCityMapper
import sh.zachwal.dailygames.results.gamemapper.CardleMapper
import sh.zachwal.dailygames.results.gamemapper.FlagleMapper
import sh.zachwal.dailygames.results.gamemapper.FramedMapper
import sh.zachwal.dailygames.results.gamemapper.GameMapper
import sh.zachwal.dailygames.results.gamemapper.GeoGridMapper
import sh.zachwal.dailygames.results.gamemapper.GeocirclesMapper
import sh.zachwal.dailygames.results.gamemapper.PinpointMapper
import sh.zachwal.dailygames.results.gamemapper.SizeItUpMapper
import sh.zachwal.dailygames.results.gamemapper.Top5Mapper
import sh.zachwal.dailygames.results.gamemapper.TradleMapper
import sh.zachwal.dailygames.results.gamemapper.TravleMapper
import sh.zachwal.dailygames.results.gamemapper.WorldleMapper
import java.time.Clock
import java.util.concurrent.Executors

class ApplicationModule : AbstractModule() {

    override fun configure() {
        bind(Clock::class.java).toInstance(Clock.systemUTC())
        bind(ObjectMapper::class.java).toInstance(jacksonObjectMapper())

        val mapperBinder = Multibinder.newSetBinder(binder(), GameMapper::class.java)
        mapperBinder.addBinding().to(WorldleMapper::class.java)
        mapperBinder.addBinding().to(TradleMapper::class.java)
        mapperBinder.addBinding().to(TravleMapper::class.java)
        mapperBinder.addBinding().to(Top5Mapper::class.java)
        mapperBinder.addBinding().to(FlagleMapper::class.java)
        mapperBinder.addBinding().to(PinpointMapper::class.java)
        mapperBinder.addBinding().to(GeocirclesMapper::class.java)
        mapperBinder.addBinding().to(FramedMapper::class.java)
        mapperBinder.addBinding().to(GeoGridMapper::class.java)
        mapperBinder.addBinding().to(BandleMapper::class.java)
        mapperBinder.addBinding().to(BracketCityMapper::class.java)
        mapperBinder.addBinding().to(SizeItUpMapper::class.java)
        mapperBinder.addBinding().to(CardleMapper::class.java)
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
