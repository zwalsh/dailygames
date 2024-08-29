package sh.zachwal.dailygames.guice

import com.google.inject.AbstractModule
import com.google.inject.Provides
import com.google.inject.Singleton
import com.google.inject.name.Named
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.asCoroutineDispatcher
import java.time.Clock
import java.util.concurrent.Executors

class ApplicationModule : AbstractModule() {

    override fun configure() {
        bind(Clock::class.java).toInstance(Clock.systemUTC())
    }

    @Provides
    @Singleton
    @Named("presserDispatcher")
    fun presserDispatcher(): CoroutineDispatcher = Executors.newFixedThreadPool(4)
        .asCoroutineDispatcher()
}
