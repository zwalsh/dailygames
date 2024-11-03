package sh.zachwal.dailygames.results.gameinfo

import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import java.time.LocalDate

class SerializeGameInfoTest {

    private val objectMapper = jacksonObjectMapper()
        .registerModule(JavaTimeModule())

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
            PinpointInfo(
                puzzleNumber = 1,
                score = 100,
                shareTextNoLink = "I got 100% on Pinpoint puzzle 1!",
            ),
            GeocirclesInfo(
                puzzleNumber = 1,
                score = 100,
                shareTextNoLink = "I got 100% on Geocircles puzzle 1!",
            ),
            TradleInfo(
                puzzleNumber = 1,
                score = 100,
                shareTextNoLink = "I got 100% on Tradle puzzle 1!",
            ),
            FlagleInfo(
                puzzleNumber = 1,
                score = 100,
                shareTextNoLink = "I got 100% on Flagle puzzle 1!",
                date = LocalDate.of(2022, 1, 1),
            ),
        )
    }
}
