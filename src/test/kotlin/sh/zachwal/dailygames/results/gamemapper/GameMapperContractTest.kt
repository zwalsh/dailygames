package sh.zachwal.dailygames.results.gamemapper

import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.DynamicTest
import org.junit.jupiter.api.TestFactory
import sh.zachwal.dailygames.db.jdbi.User

abstract class GameMapperContractTest {
    abstract val mapper: GameMapper
    abstract val fixtures: List<String>
    private val testUser = User(id = 1L, username = "testuser", hashedPassword = "hash")

    @TestFactory
    fun `each fixture matches and extracts without throwing`() =
        fixtures.map { text ->
            DynamicTest.dynamicTest(text.lines().first { it.isNotBlank() }) {
                assertThat(mapper.matches(text)).isTrue()
                mapper.extract(text, testUser)
            }
        }
}
