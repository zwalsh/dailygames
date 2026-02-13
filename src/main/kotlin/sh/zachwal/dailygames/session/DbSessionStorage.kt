package sh.zachwal.dailygames.session

import io.ktor.server.sessions.SessionStorage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.slf4j.LoggerFactory
import sh.zachwal.dailygames.db.dao.SessionDAO
import sh.zachwal.dailygames.db.jdbi.Session
import java.time.Instant
import javax.inject.Inject

class DbSessionStorage @Inject constructor(private val sessionDAO: SessionDAO) : SessionStorage {
    private val logger = LoggerFactory.getLogger(DbSessionStorage::class.java)

    override suspend fun invalidate(id: String) {
        logger.debug("Clearing {}", id)
        withContext(Dispatchers.IO) {
            sessionDAO.deleteSession(id)
        }
    }

    override suspend fun read(id: String): String {
        logger.debug("Reading {}", id)
        return withContext(Dispatchers.IO) {
            val bytes = sessionDAO.getById(id)?.data
                ?: throw NoSuchElementException("No session with id $id")
            bytes.decodeToString()
        }
    }

    override suspend fun write(id: String, value: String) {
        logger.debug("Writing {}", id)
        withContext(Dispatchers.IO) {
            val bytes = value.encodeToByteArray()
            sessionDAO.createOrUpdateSession(Session(id, bytes, Instant.now().plus(SESSION_MAX_DURATION)))
        }
    }
}
