package sh.zachwal.dailygames.home

import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.Test
import sh.zachwal.dailygames.db.jdbi.puzzle.Game
import sh.zachwal.dailygames.db.jdbi.puzzle.PuzzleResult
import sh.zachwal.dailygames.leaderboard.PointCalculator
import sh.zachwal.dailygames.results.resultinfo.FlagleInfo
import sh.zachwal.dailygames.results.resultinfo.FramedInfo
import sh.zachwal.dailygames.results.resultinfo.GeoGridInfo
import sh.zachwal.dailygames.results.resultinfo.GeocirclesInfo
import sh.zachwal.dailygames.results.resultinfo.PinpointInfo
import sh.zachwal.dailygames.results.resultinfo.Top5Info
import sh.zachwal.dailygames.results.resultinfo.TradleInfo
import sh.zachwal.dailygames.results.resultinfo.TravleInfo
import sh.zachwal.dailygames.results.resultinfo.WorldleInfo
import java.time.Instant

class ShareLineMapperTest {

    private val mapper = ShareLineMapper(
        pointCalculator = PointCalculator()
    )

    private val worldleResult = PuzzleResult(
        id = 1,
        userId = 1,
        game = Game.WORLDLE,
        puzzleNumber = 123,
        puzzleDate = null,
        instantSubmitted = Instant.now(),
        score = 1,
        shareText = "share text",
        resultInfo = WorldleInfo(
            percentage = 100,
        ),
    )

    @Test
    fun `maps worldle perfect`() {
        val shareLine = mapper.mapToShareLine(worldleResult)

        assertThat(shareLine).isEqualTo("${Game.WORLDLE.emoji()} Worldle #123 1/6 ${Game.WORLDLE.perfectEmoji()}")
    }

    @Test
    fun `maps worldle non-perfect`() {
        val shareLine = mapper.mapToShareLine(worldleResult.copy(score = 4))

        assertThat(shareLine).isEqualTo("${Game.WORLDLE.emoji()} Worldle #123 4/6")
    }

    @Test
    fun `for Worldle fail, includes percentage`() {
        // Given
        val failResult = worldleResult.copy(score = 7, resultInfo = WorldleInfo(percentage = 50))

        // When
        val shareLine = mapper.mapToShareLine(failResult)

        // Then
        assertThat(shareLine).isEqualTo("${Game.WORLDLE.emoji()} Worldle #123 X/6 (50%)")
    }

    private val tradleResult = worldleResult.copy(
        game = Game.TRADLE,
        score = 1,
        resultInfo = TradleInfo,
    )

    @Test
    fun `maps tradle line`() {
        val shareLine = mapper.mapToShareLine(tradleResult.copy(score = 2))

        assertThat(shareLine).isEqualTo("${Game.TRADLE.emoji()} Tradle #123 2/6")
    }

    @Test
    fun `maps tradle perfect`() {
        val perfectResult = tradleResult.copy(score = 1)

        val shareLine = mapper.mapToShareLine(perfectResult)

        assertThat(shareLine).isEqualTo("${Game.TRADLE.emoji()} Tradle #123 1/6 ${Game.TRADLE.perfectEmoji()}")
    }

    @Test
    fun `maps tradle fail`() {
        val failResult = tradleResult.copy(score = 7)

        val shareLine = mapper.mapToShareLine(failResult)

        assertThat(shareLine).isEqualTo("${Game.TRADLE.emoji()} Tradle #123 X/6")
    }

    private val travleResult = worldleResult.copy(
        game = Game.TRAVLE,
        score = 0,
        resultInfo = TravleInfo(
            numGuesses = 6,
            numIncorrect = 0,
            numPerfect = 6,
            numHints = 0,
        ),
    )

    private val flagleResult = worldleResult.copy(
        game = Game.FLAGLE,
        score = 1,
        resultInfo = FlagleInfo
    )

    @Test
    fun `maps flagle line`() {
        val shareLine = mapper.mapToShareLine(flagleResult.copy(score = 2))

        assertThat(shareLine).isEqualTo("${Game.FLAGLE.emoji()} Flagle #123 2/6")
    }

    @Test
    fun `maps flagle perfect`() {
        val perfectResult = flagleResult.copy(score = 1)

        val shareLine = mapper.mapToShareLine(perfectResult)

        assertThat(shareLine).isEqualTo("${Game.FLAGLE.emoji()} Flagle #123 1/6 ${Game.FLAGLE.perfectEmoji()}")
    }

    @Test
    fun `maps flagle fail`() {
        val failResult = flagleResult.copy(score = 7)

        val shareLine = mapper.mapToShareLine(failResult)

        assertThat(shareLine).isEqualTo("${Game.FLAGLE.emoji()} Flagle #123 X/6")
    }

    @Test
    fun `maps travle perfect`() {
        val shareLine = mapper.mapToShareLine(travleResult)

        assertThat(shareLine).isEqualTo("${Game.TRAVLE.emoji()} Travle #123 +0 ${Game.TRAVLE.perfectEmoji()}")
    }

    @Test
    fun `maps travle plus zero non-perfect`() {
        val result = travleResult.copy(
            score = 0,
            resultInfo = (travleResult.info<TravleInfo>()).copy(numPerfect = 5)
        )

        val shareLine = mapper.mapToShareLine(result)

        assertThat(shareLine).isEqualTo("${Game.TRAVLE.emoji()} Travle #123 +0")
    }

    @Test
    fun `maps travle plus one`() {
        val result = travleResult.copy(
            score = 1,
            resultInfo = (travleResult.info<TravleInfo>()).copy(
                numGuesses = 7,
                numPerfect = 6,
                numIncorrect = 1,
            )
        )

        val shareLine = mapper.mapToShareLine(result)

        assertThat(shareLine).isEqualTo("${Game.TRAVLE.emoji()} Travle #123 +1")
    }

    @Test
    fun `maps travle with hints`() {
        val result = travleResult.copy(
            score = 1,
            resultInfo = (travleResult.info<TravleInfo>()).copy(
                numGuesses = 7,
                numPerfect = 6,
                numIncorrect = 1,
                numHints = 2,
            )
        )

        val shareLine = mapper.mapToShareLine(result)

        assertThat(shareLine).isEqualTo("${Game.TRAVLE.emoji()} Travle #123 +1 (2 hints)")
    }

    @Test
    fun `maps travle with negative score`() {
        val result = travleResult.copy(
            score = -1,
            resultInfo = (travleResult.info<TravleInfo>()).copy(
                numGuesses = 10,
                numPerfect = 6,
                numIncorrect = 4,
            )
        )

        val shareLine = mapper.mapToShareLine(result)

        assertThat(shareLine).isEqualTo("${Game.TRAVLE.emoji()} Travle #123 (1 away)")
    }

    @Test
    fun `maps travle with negative score and one hint`() {
        val result = travleResult.copy(
            score = -1,
            resultInfo = (travleResult.info<TravleInfo>()).copy(
                numGuesses = 10,
                numPerfect = 6,
                numIncorrect = 4,
                numHints = 1,
            )
        )

        val shareLine = mapper.mapToShareLine(result)

        assertThat(shareLine).isEqualTo("${Game.TRAVLE.emoji()} Travle #123 (1 away) (1 hint)")
    }

    private val top5Result = worldleResult.copy(
        game = Game.TOP5,
        score = 5,
        resultInfo = Top5Info(
            numGuesses = 5,
            numCorrect = 5,
            isPerfect = true,
        )
    )

    @Test
    fun `maps top5 perfect`() {
        val shareLine = mapper.mapToShareLine(top5Result)

        assertThat(shareLine).isEqualTo("${Game.TOP5.emoji()} Top 5 #123 5/5 \uD83C\uDF08")
    }

    @Test
    fun `maps top5 five correct, no misses, but not perfect`() {
        val result = top5Result.copy(
            resultInfo = (top5Result.info<Top5Info>()).copy(
                isPerfect = false
            )
        )

        val shareLine = mapper.mapToShareLine(result)

        assertThat(shareLine).isEqualTo("${Game.TOP5.emoji()} Top 5 #123 5/5")
    }

    @Test
    fun `maps top5 with five correct but some misses`() {
        val result = top5Result.copy(
            resultInfo = (top5Result.info<Top5Info>()).copy(
                numCorrect = 5,
                numGuesses = 6,
                isPerfect = false,
            )
        )

        val shareLine = mapper.mapToShareLine(result)

        assertThat(shareLine).isEqualTo("${Game.TOP5.emoji()} Top 5 #123 5/5 (1 wrong)")
    }

    @Test
    fun `maps top5 with misses`() {
        val result = top5Result.copy(
            resultInfo = (top5Result.info<Top5Info>()).copy(
                numCorrect = 4,
                numGuesses = 10,
                isPerfect = false,
            )
        )

        val shareLine = mapper.mapToShareLine(result)

        assertThat(shareLine).isEqualTo("${Game.TOP5.emoji()} Top 5 #123 4/5")
    }

    private val pinpointResult = worldleResult.copy(
        game = Game.PINPOINT,
        score = 1,
        resultInfo = PinpointInfo,
    )

    @Test
    fun `maps pinpoint perfect`() {
        val shareLine = mapper.mapToShareLine(pinpointResult)

        assertThat(shareLine).isEqualTo("${Game.PINPOINT.emoji()} Pinpoint #123 1/5 ${Game.PINPOINT.perfectEmoji()}")
    }

    @Test
    fun `maps pinpoint non-perfect`() {
        val shareLine = mapper.mapToShareLine(pinpointResult.copy(score = 4))

        assertThat(shareLine).isEqualTo("${Game.PINPOINT.emoji()} Pinpoint #123 4/5")
    }

    @Test
    fun `maps pinpoint fail`() {
        val failResult = pinpointResult.copy(score = 6)

        val shareLine = mapper.mapToShareLine(failResult)

        assertThat(shareLine).isEqualTo("${Game.PINPOINT.emoji()} Pinpoint #123 X/5")
    }

    private val geocirclesResult = worldleResult.copy(
        game = Game.GEOCIRCLES,
        score = 10,
        resultInfo = GeocirclesInfo,
    )

    @Test
    fun `maps geocircles line perfect`() {
        val shareLine = mapper.mapToShareLine(geocirclesResult)

        assertThat(shareLine).isEqualTo("${Game.GEOCIRCLES.emoji()} Geocircles #123 5/5 \uD83C\uDFAF")
    }

    @Test
    fun `maps geocircles line lives left`() {
        val result = geocirclesResult.copy(score = 8)

        val shareLine = mapper.mapToShareLine(result)

        assertThat(shareLine).isEqualTo("${Game.GEOCIRCLES.emoji()} Geocircles #123 5/5 (2 wrong)")
    }

    @Test
    fun `maps geocircles line some right`() {
        val result = geocirclesResult.copy(score = 4)

        val shareLine = mapper.mapToShareLine(result)

        assertThat(shareLine).isEqualTo("${Game.GEOCIRCLES.emoji()} Geocircles #123 4/5")
    }

    @Test
    fun `maps geocircles line none right`() {
        val result = geocirclesResult.copy(score = 0)

        val shareLine = mapper.mapToShareLine(result)

        assertThat(shareLine).isEqualTo("${Game.GEOCIRCLES.emoji()} Geocircles #123 0/5")
    }

    private val framedResult = worldleResult.copy(
        game = Game.FRAMED,
        score = 2,
        resultInfo = FramedInfo,
    )

    @Test
    fun `maps framed line`() {
        val shareLine = mapper.mapToShareLine(framedResult)

        assertThat(shareLine).isEqualTo("${Game.FRAMED.emoji()} Framed #123 2/6")
    }

    @Test
    fun `maps framed line perfect`() {
        val perfectResult = framedResult.copy(score = 1)

        val shareLine = mapper.mapToShareLine(perfectResult)

        assertThat(shareLine).isEqualTo("${Game.FRAMED.emoji()} Framed #123 1/6 ${Game.FRAMED.perfectEmoji()}")
    }

    @Test
    fun `maps framed line fail`() {
        val failResult = framedResult.copy(score = 7)

        val shareLine = mapper.mapToShareLine(failResult)

        assertThat(shareLine).isEqualTo("${Game.FRAMED.emoji()} Framed #123 X/6")
    }

    private val geoGridResult = worldleResult.copy(
        game = Game.GEOGRID,
        score = 9,
        resultInfo = GeoGridInfo(
            score = 123.4,
            rank = 123,
            rankOutOf = 5555,
            numCorrect = 9,
        ),
    )

    @Test
    fun `maps geogrid line`() {
        val shareLine = mapper.mapToShareLine(geoGridResult)
        assertThat(shareLine).isEqualTo("${Game.GEOGRID.emoji()} GeoGrid #123 9/9 (123.4)")
    }

    @Test
    fun `maps geogrid line zero`() {
        val result = geoGridResult.copy(
            score = 0,
            resultInfo = geoGridResult.info<GeoGridInfo>().copy(
                numCorrect = 0,
                score = 900.0,
            )
        )

        val shareLine = mapper.mapToShareLine(result)
        assertThat(shareLine).isEqualTo("${Game.GEOGRID.emoji()} GeoGrid #123 0/9 (900.0)")
    }
    
    @Test
    fun `maps geogrid line 8 of 9`() {
        val result = geoGridResult.copy(
            score = 8,
            resultInfo = geoGridResult.info<GeoGridInfo>().copy(
                numCorrect = 8,
                score = 200.5,
            )
        )

        val shareLine = mapper.mapToShareLine(result)
        assertThat(shareLine).isEqualTo("${Game.GEOGRID.emoji()} GeoGrid #123 8/9 (200.5)")
    }
    
    @Test
    fun `keeps max one decimal point of geogrid score`() {
        val result = geoGridResult.copy(
            resultInfo = geoGridResult.info<GeoGridInfo>().copy(
                score = 200.55,
            )
        )

        val shareLine = mapper.mapToShareLine(result)
        assertThat(shareLine).endsWith("(200.6)")
    }
}
