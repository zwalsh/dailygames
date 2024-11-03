package sh.zachwal.dailygames.results.gameinfo

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource

class SerializeGameInfoTest {

    private val objectMapper = jacksonObjectMapper()

    @Test
    fun `can serialize and deserialize a game info object into a specific type`() {
        val worldleInfo = WorldleInfo(
            percentage = 100,
        )

        val worldleInfoJson = objectMapper.writeValueAsString(worldleInfo)

        val deserializedWorldleInfo = objectMapper.readValue<GameInfo>(worldleInfoJson)

        assertThat(deserializedWorldleInfo).isInstanceOf(WorldleInfo::class.java)
        assertThat(deserializedWorldleInfo).isEqualTo(worldleInfo)
    }

    @ParameterizedTest
    @MethodSource("sh.zachwal.dailygames.results.gameinfo.SerializeGameInfoTest#gameInfoObjects")
    fun `can serialize and deserialize different game info objects`(gameInfo: GameInfo) {
        val serialized = objectMapper.writeValueAsString(gameInfo)

        val deserialized: GameInfo = objectMapper.readValue(serialized)

        assertThat(deserialized).isEqualTo(gameInfo)
    }

    companion object {
        @JvmStatic
        fun gameInfoObjects() = listOf(
            WorldleInfo(
                percentage = 100,
            ),
            PinpointInfo,
            GeocirclesInfo,
            TradleInfo,
            FlagleInfo,
            TravleInfo(
                numGuesses = 6,
                numIncorrect = 0,
                numPerfect = 5,
                numHints = 0,
            ),
            Top5Info(
                numGuesses = 5,
                numCorrect = 5,
                isPerfect = true,
            ),
        )
    }
}
