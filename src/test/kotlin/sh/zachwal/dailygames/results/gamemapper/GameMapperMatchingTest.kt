package sh.zachwal.dailygames.results.gamemapper

import com.google.common.truth.Truth.assertThat
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Test
import sh.zachwal.dailygames.users.UserPreferencesService
import java.time.Clock
import java.time.ZoneId

class GameMapperMatchingTest {
    private val userPreferencesService = mockk<UserPreferencesService> {
        every { getTimeZone(any()) } returns ZoneId.of("America/New_York")
    }
    private val mappers: List<GameMapper> = allGameMappers(
        clock = Clock.systemUTC(),
        userPreferencesService = userPreferencesService,
    ).toList()

    private val allFixtures: List<String> =
        WorldleFixtures.ALL +
            TradleFixtures.ALL +
            TravleFixtures.ALL +
            Top5Fixtures.ALL +
            FlagleFixtures.ALL +
            PinpointFixtures.ALL +
            GeocirclesFixtures.ALL +
            FramedFixtures.ALL +
            GeoGridFixtures.ALL +
            BandleFixtures.ALL +
            BracketCityFixtures.ALL +
            SizeItUpFixtures.ALL +
            CardleFixtures.ALL +
            KrillionFixtures.ALL

    @Test
    fun `every fixture matches exactly one mapper`() {
        allFixtures.forEach { text ->
            assertThat(mappers.filter { it.matches(text) }).hasSize(1)
        }
    }

    @Test
    fun `every game has exactly one mapper`() {
        assertThat(mappers.map { it.game }.toSet()).hasSize(mappers.size)
    }
}
