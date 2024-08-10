package sh.zachwal.dailygames.db.dao

import com.google.common.truth.Truth.assertThat
import org.jdbi.v3.core.Jdbi
import org.jdbi.v3.sqlobject.kotlin.onDemand
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import sh.zachwal.dailygames.db.DatabaseExtension

@ExtendWith(DatabaseExtension::class)

class UserDAOTest(jdbi: Jdbi) {
    private val userDAO: UserDAO = jdbi.onDemand()

    @Test
    fun `can create and retrieve a user`() {
        userDAO.createUser("testy", "someHash")

        val user = userDAO.getByUsername("testy")

        assertThat(user).isNotNull()
        assertThat(user!!.username).isEqualTo("testy")
        assertThat(user.hashedPassword).isEqualTo("someHash")
    }

    @Test
    fun `can list users`() {
        val initialList = userDAO.listUsers()

        assertThat(initialList).isEmpty()

        userDAO.createUser("one", "one")
        userDAO.createUser("two", "two")

        val twoUsers = userDAO.listUsers()

        assertThat(twoUsers).hasSize(2)
    }
}