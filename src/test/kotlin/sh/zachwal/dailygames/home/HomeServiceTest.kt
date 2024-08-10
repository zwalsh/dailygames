package sh.zachwal.dailygames.home

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import sh.zachwal.dailygames.db.jdbi.User

class HomeServiceTest {

    private val homeService = HomeService()

    @Test
    fun `creates home view with username of user`() {
        val user = User(id = 1L, username = "zach", hashedPassword = "123abc==")
        val view = homeService.homeView(user)

        assertEquals(user.username, view.username)
    }
}
