package sh.zachwal.dailygames.guice

import com.google.inject.AbstractModule
import com.google.inject.Provides
import com.google.inject.Singleton
import org.jdbi.v3.core.Jdbi
import org.jdbi.v3.core.kotlin.KotlinPlugin
import org.jdbi.v3.postgres.PostgresPlugin
import org.jdbi.v3.sqlobject.kotlin.KotlinSqlObjectPlugin
import org.jdbi.v3.sqlobject.kotlin.onDemand
import sh.zachwal.dailygames.db.dao.ChatDAO
import sh.zachwal.dailygames.db.dao.SessionDAO
import sh.zachwal.dailygames.db.dao.UserDAO
import sh.zachwal.dailygames.db.dao.UserPreferencesDAO
import sh.zachwal.dailygames.db.dao.UserRoleDAO
import sh.zachwal.dailygames.db.dao.game.FlagleDAO
import sh.zachwal.dailygames.db.dao.game.GameDAO
import sh.zachwal.dailygames.db.dao.game.PinpointDAO
import sh.zachwal.dailygames.db.dao.game.PuzzleDAO
import sh.zachwal.dailygames.db.dao.game.Top5DAO
import sh.zachwal.dailygames.db.dao.game.TradleDAO
import sh.zachwal.dailygames.db.dao.game.TravleDAO
import sh.zachwal.dailygames.db.dao.game.WorldleDAO
import javax.sql.DataSource

class JdbiModule : AbstractModule() {

    @Provides
    @Singleton
    fun jdbi(ds: DataSource): Jdbi {
        return Jdbi.create(ds).installPlugin(KotlinPlugin())
            .installPlugin(PostgresPlugin())
            .installPlugin(KotlinSqlObjectPlugin())
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
    fun worldleDao(jdbi: Jdbi): WorldleDAO = jdbi.onDemand()

    @Provides
    fun tradleDao(jdbi: Jdbi): TradleDAO = jdbi.onDemand()

    @Provides
    fun travleDao(jdbi: Jdbi): TravleDAO = jdbi.onDemand()

    @Provides
    fun top5Dao(jdbi: Jdbi): Top5DAO = jdbi.onDemand()

    @Provides
    fun flagleDao(jdbi: Jdbi): FlagleDAO = jdbi.onDemand()

    @Provides
    fun chatDao(jdbi: Jdbi): ChatDAO = jdbi.onDemand()

    @Provides
    fun userPreferencesDao(jdbi: Jdbi): UserPreferencesDAO = jdbi.onDemand()

    @Provides
    fun pinpointDao(jdbi: Jdbi): PinpointDAO = jdbi.onDemand()
}
