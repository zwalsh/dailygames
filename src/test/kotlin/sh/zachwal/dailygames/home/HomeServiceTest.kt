package sh.zachwal.dailygames.home

import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import sh.zachwal.dailygames.db.jdbi.User
import sh.zachwal.dailygames.results.ResultService

class HomeServiceTest {

    private val resultService = mockk<ResultService> {
        every { resultFeed(any()) } returns emptyList()
    }
    private val homeService = HomeService(resultService)

    @Test
    fun `creates home view with username of user`() {
        val user = User(id = 1L, username = "zach", hashedPassword = "123abc==")
        val view = homeService.homeView(user)

        assertEquals(user.username, view.username)
    }
}
