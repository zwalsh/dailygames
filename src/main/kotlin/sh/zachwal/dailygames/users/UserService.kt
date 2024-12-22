package sh.zachwal.dailygames.users

import io.ktor.util.error
import org.jdbi.v3.core.statement.UnableToExecuteStatementException
import org.mindrot.jbcrypt.BCrypt
import org.slf4j.LoggerFactory
import sh.zachwal.dailygames.db.dao.UserDAO
import sh.zachwal.dailygames.db.dao.UserPreferencesDAO
import sh.zachwal.dailygames.db.jdbi.User
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject

class UserService @Inject constructor(
    private val userDAO: UserDAO,
    private val userPreferencesDAO: UserPreferencesDAO,
) {

    private val logger = LoggerFactory.getLogger(UserService::class.java)

    fun getUser(username: String): User? {
        return try {
            userDAO.getByUsername(username)
        } catch (e: Exception) {
            logger.info("Error retrieving user $username.", e)
            null
        }
    }

    fun getUser(id: Long): User? {
        return try {
            userDAO.getById(id)
        } catch (e: Exception) {
            logger.info("User with id $id could not be found", e)
            null
        }
    }

    fun checkUser(username: String, password: String): User? = getUser(username)?.takeIf {
        try {
            BCrypt.checkpw(password, it.hashedPassword)
        } catch (e: Exception) {
            logger.warn("Failed to check password for user $username", e)
            false
        }
    }

    fun createUser(username: String, password: String): User? {
        val hash = BCrypt.hashpw(password, BCrypt.gensalt())
        val user = try {
            userDAO.createUser(username, hash)
        } catch (e: UnableToExecuteStatementException) {
            logger.error(e)
            null
        }

        if (user != null) {
            userPreferencesDAO.createDefaultPreferences(user.id)
        }

        return user
    }

    fun list(): List<User> = userDAO.listUsers()

    // TODO use a real cache library, maybe
    private val userNameCache = ConcurrentHashMap<Long, String?>()

    fun getUsernameCached(userId: Long): String? {
        return userNameCache.computeIfAbsent(userId) { getUser(it)?.username }
    }

    fun setPassword(user: User, newPassword: String) {
        val newHash = BCrypt.hashpw(newPassword, BCrypt.gensalt())
        userDAO.updatePassword(user.id, newHash)
    }

    fun userChangePassword(user: User, currentPassword: String, newPassword: String, repeatNewPassword: String): ChangePasswordResult {
        if (newPassword != repeatNewPassword) {
            return ChangePasswordFailure("Passwords do not match")
        }
        if (!BCrypt.checkpw(currentPassword, user.hashedPassword)) {
            return ChangePasswordFailure("Current password is incorrect")
        }
        setPassword(user, newPassword)
        return ChangePasswordSuccess
    }
}
