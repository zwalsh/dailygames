package sh.zachwal.dailygames.results.resultinfo

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource

/**
 * We have stored game info objects in a database. We must be backwards-compatible & able to deserialize any of them
 * even as we add fields.
 */
class DeserializeStoredPuzzleResultInfoTest {

    private val objectMapper = jacksonObjectMapper()

    @ParameterizedTest
    @MethodSource("sh.zachwal.dailygames.results.resultinfo.DeserializeStoredResultInfoTest#arguments")
    fun `can deserialize stored, serialized ResultInfo object`(serialized: String, resultInfo: ResultInfo) {
        assertThat(objectMapper.readValue<ResultInfo>(serialized)).isEqualTo(resultInfo)
    }

    companion object {
        // Add here any time the format of these changes
        @JvmStatic
        fun arguments(): List<Arguments> = listOf(
            Arguments.of(
                "{\"type\":\"worldle\",\"percentage\":100}", WorldleInfo(percentage = 100)
            ),
            Arguments.of(
                "{\"type\":\"pinpoint\"}", PinpointInfo
            ),
            Arguments.of(
                "{\"type\":\"geocircles\"}", GeocirclesInfo
            ),
            Arguments.of(
                "{\"type\":\"tradle\"}", TradleInfo
            ),
            Arguments.of(
                "{\"type\":\"flagle\"}", FlagleInfo
            ),
            Arguments.of(
                "{\"type\":\"travle\",\"numGuesses\":6,\"numIncorrect\":0,\"numPerfect\":5,\"numHints\":0}",
                TravleInfo(
                    numGuesses = 6,
                    numIncorrect = 0,
                    numPerfect = 5,
                    numHints = 0,
                )
            ),
            Arguments.of(
                "{\"type\":\"top5\",\"numGuesses\":5,\"numCorrect\":5,\"isPerfect\":true}",
                Top5Info(
                    numGuesses = 5,
                    numCorrect = 5,
                    isPerfect = true,
                )
            ),
        )
    }
}
