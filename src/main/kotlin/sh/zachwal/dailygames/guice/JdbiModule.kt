package sh.zachwal.dailygames.guice

import com.fasterxml.jackson.databind.ObjectMapper
import com.google.inject.AbstractModule
import com.google.inject.Provides
import com.google.inject.Singleton
import org.jdbi.v3.core.Jdbi
import org.jdbi.v3.core.kotlin.KotlinPlugin
import org.jdbi.v3.jackson2.Jackson2Config
import org.jdbi.v3.jackson2.Jackson2Plugin
import org.jdbi.v3.postgres.PostgresPlugin
import org.jdbi.v3.sqlobject.kotlin.KotlinSqlObjectPlugin
import org.jdbi.v3.sqlobject.kotlin.onDemand
import sh.zachwal.dailygames.db.dao.ChatDAO
import sh.zachwal.dailygames.db.dao.SessionDAO
import sh.zachwal.dailygames.db.dao.UserDAO
import sh.zachwal.dailygames.db.dao.UserPreferencesDAO
import sh.zachwal.dailygames.db.dao.UserRoleDAO
import sh.zachwal.dailygames.db.dao.game.GameDAO
import sh.zachwal.dailygames.db.dao.game.PuzzleDAO
import sh.zachwal.dailygames.db.dao.game.PuzzleResultDAO
import javax.sql.DataSource

class JdbiModule : AbstractModule() {

    @Provides
    @Singleton
    fun jdbi(ds: DataSource, objectMapper: ObjectMapper): Jdbi {
        val jdbi = Jdbi.create(ds)
            .installPlugin(KotlinPlugin())
            .installPlugin(PostgresPlugin())
            .installPlugin(KotlinSqlObjectPlugin())
            .installPlugin(Jackson2Plugin())

        val config = jdbi.getConfig(Jackson2Config::class.java)
        config.setMapper(objectMapper)

        return jdbi
    }

    @Provides
    fun userDao(jdbi: Jdbi): UserDAO = jdbi.onDemand()

    @Provides
    fun sessionDao(jdbi: Jdbi): SessionDAO = jdbi.onDemand()

    @Provides
    fun roleDao(jdbi: Jdbi): UserRoleDAO = jdbi.onDemand()

    @Provides
    fun gameDao(jdbi: Jdbi): GameDAO = jdbi.onDemand()

    @Provides
    fun puzzleDao(jdbi: Jdbi): PuzzleDAO = jdbi.onDemand()

    @Provides
    fun chatDao(jdbi: Jdbi): ChatDAO = jdbi.onDemand()

    @Provides
    fun userPreferencesDao(jdbi: Jdbi): UserPreferencesDAO = jdbi.onDemand()

    @Provides
    fun resultDao(jdbi: Jdbi): PuzzleResultDAO = jdbi.onDemand()
}
