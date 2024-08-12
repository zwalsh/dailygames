package sh.zachwal.dailygames.db.extension

import org.jdbi.v3.core.Jdbi
import org.jdbi.v3.sqlobject.kotlin.onDemand
import sh.zachwal.dailygames.db.dao.UserDAO
import sh.zachwal.dailygames.db.jdbi.User

class Fixtures(
    jdbi: Jdbi
) {
    private val userDAO: UserDAO = jdbi.onDemand()

    lateinit var zach: User
    lateinit var jackie: User

    fun runFixtures() {
        zach = userDAO.createUser("zach", "hashedPassword")!!.also {
            println("Created user: $it")
        }
        jackie = userDAO.createUser("jackie", "hashedPassword")!!
    }
}
