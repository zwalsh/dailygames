package sh.zachwal.dailygames.home

import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.Test
import sh.zachwal.dailygames.db.jdbi.puzzle.Game
import sh.zachwal.dailygames.db.jdbi.puzzle.PuzzleResult
import sh.zachwal.dailygames.results.resultinfo.FlagleInfo
import sh.zachwal.dailygames.results.resultinfo.GeocirclesInfo
import sh.zachwal.dailygames.results.resultinfo.PinpointInfo
import sh.zachwal.dailygames.results.resultinfo.Top5Info
import sh.zachwal.dailygames.results.resultinfo.TradleInfo
import sh.zachwal.dailygames.results.resultinfo.TravleInfo
import sh.zachwal.dailygames.results.resultinfo.WorldleInfo
import java.time.Instant

class ShareLineMapperTest {

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
        val shareLine = ShareLineMapper().mapToShareLine(worldleResult)

        assertThat(shareLine).isEqualTo("${Game.WORLDLE.emoji()} Worldle #123 1/6 ${Game.WORLDLE.perfectEmoji()}")
    }

    @Test
    fun `maps worldle non-perfect`() {
        val shareLine = ShareLineMapper().mapToShareLine(worldleResult.copy(score = 4))

        assertThat(shareLine).isEqualTo("${Game.WORLDLE.emoji()} Worldle #123 4/6")
    }

    @Test
    fun `for Worldle fail, includes percentage`() {
        // Given
        val failResult = worldleResult.copy(score = 7, resultInfo = WorldleInfo(percentage = 50))

        // When
        val shareLine = ShareLineMapper().mapToShareLine(failResult)

        // Then
        assertThat(shareLine).isEqualTo("${Game.WORLDLE.emoji()} Worldle #123 X/6 (50%)")
    }

    private val tradleResult = PuzzleResult(
        id = 1,
        userId = 1,
        game = Game.TRADLE,
        puzzleNumber = 123,
        puzzleDate = null,
        instantSubmitted = Instant.now(),
        score = 1,
        shareText = "share text",
        resultInfo = TradleInfo,
    )

    @Test
    fun `maps tradle line`() {
        val shareLine = ShareLineMapper().mapToShareLine(tradleResult.copy(score = 2))

        assertThat(shareLine).isEqualTo("${Game.TRADLE.emoji()} Tradle #123 2/6")
    }

    @Test
    fun `maps tradle perfect`() {
        val perfectResult = tradleResult.copy(score = 1)

        val shareLine = ShareLineMapper().mapToShareLine(perfectResult)

        assertThat(shareLine).isEqualTo("${Game.TRADLE.emoji()} Tradle #123 1/6 ${Game.TRADLE.perfectEmoji()}")
    }

    @Test
    fun `maps tradle fail`() {
        val failResult = tradleResult.copy(score = 7)

        val shareLine = ShareLineMapper().mapToShareLine(failResult)

        assertThat(shareLine).isEqualTo("${Game.TRADLE.emoji()} Tradle #123 X/6")
    }

    private val travleResult = PuzzleResult(
        id = 1L,
        userId = 1L,
        game = Game.TRAVLE,
        puzzleNumber = 123,
        puzzleDate = null,
        instantSubmitted = Instant.now(),
        score = 0,
        shareText = "share text",
        resultInfo = TravleInfo(
            numGuesses = 6,
            numIncorrect = 0,
            numPerfect = 6,
            numHints = 0,
        ),
    )

    private val flagleResult = PuzzleResult(
        id = 1,
        userId = 1,
        game = Game.FLAGLE,
        puzzleNumber = 123,
        puzzleDate = null,
        instantSubmitted = Instant.now(),
        score = 1,
        shareText = "share text",
        resultInfo = FlagleInfo
    )

    @Test
    fun `maps flagle line`() {
        val shareLine = ShareLineMapper().mapToShareLine(flagleResult.copy(score = 2))

        assertThat(shareLine).isEqualTo("${Game.FLAGLE.emoji()} Flagle #123 2/6")
    }

    @Test
    fun `maps flagle perfect`() {
        val perfectResult = flagleResult.copy(score = 1)

        val shareLine = ShareLineMapper().mapToShareLine(perfectResult)

        assertThat(shareLine).isEqualTo("${Game.FLAGLE.emoji()} Flagle #123 1/6 ${Game.FLAGLE.perfectEmoji()}")
    }

    @Test
    fun `maps flagle fail`() {
        val failResult = flagleResult.copy(score = 7)

        val shareLine = ShareLineMapper().mapToShareLine(failResult)

        assertThat(shareLine).isEqualTo("${Game.FLAGLE.emoji()} Flagle #123 X/6")
    }

    @Test
    fun `maps travle perfect`() {
        val shareLine = ShareLineMapper().mapToShareLine(travleResult)

        assertThat(shareLine).isEqualTo("${Game.TRAVLE.emoji()} Travle #123 +0 ${Game.TRAVLE.perfectEmoji()}")
    }

    @Test
    fun `maps travle plus zero non-perfect`() {
        val result = travleResult.copy(
            score = 0,
            resultInfo = (travleResult.info<TravleInfo>()).copy(numPerfect = 5)
        )

        val shareLine = ShareLineMapper().mapToShareLine(result)

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

        val shareLine = ShareLineMapper().mapToShareLine(result)

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

        val shareLine = ShareLineMapper().mapToShareLine(result)

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

        val shareLine = ShareLineMapper().mapToShareLine(result)

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

        val shareLine = ShareLineMapper().mapToShareLine(result)

        assertThat(shareLine).isEqualTo("${Game.TRAVLE.emoji()} Travle #123 (1 away) (1 hint)")
    }

    private val top5Result = PuzzleResult(
        id = 1,
        userId = 1,
        game = Game.TOP5,
        puzzleNumber = 123,
        puzzleDate = null,
        instantSubmitted = Instant.now(),
        score = 5,
        shareText = "share text",
        resultInfo = Top5Info(
            numGuesses = 5,
            numCorrect = 5,
            isPerfect = true,
        )
    )

    @Test
    fun `maps top5 perfect`() {
        val shareLine = ShareLineMapper().mapToShareLine(top5Result)

        assertThat(shareLine).isEqualTo("${Game.TOP5.emoji()} Top 5 #123 5/5 \uD83C\uDF08")
    }

    @Test
    fun `maps top5 five correct, no misses, but not perfect`() {
        val result = top5Result.copy(
            resultInfo = (top5Result.info<Top5Info>()).copy(
                isPerfect = false
            )
        )

        val shareLine = ShareLineMapper().mapToShareLine(result)

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

        val shareLine = ShareLineMapper().mapToShareLine(result)

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

        val shareLine = ShareLineMapper().mapToShareLine(result)

        assertThat(shareLine).isEqualTo("${Game.TOP5.emoji()} Top 5 #123 4/5")
    }

    private val pinpointResult = PuzzleResult(
        id = 1,
        userId = 1,
        game = Game.PINPOINT,
        puzzleNumber = 123,
        puzzleDate = null,
        instantSubmitted = Instant.now(),
        score = 1,
        shareText = "share text",
        resultInfo = PinpointInfo,
    )

    @Test
    fun `maps pinpoint perfect`() {
        val shareLine = ShareLineMapper().mapToShareLine(pinpointResult)

        assertThat(shareLine).isEqualTo("${Game.PINPOINT.emoji()} Pinpoint #123 1/5 ${Game.PINPOINT.perfectEmoji()}")
    }

    @Test
    fun `maps pinpoint non-perfect`() {
        val shareLine = ShareLineMapper().mapToShareLine(pinpointResult.copy(score = 4))

        assertThat(shareLine).isEqualTo("${Game.PINPOINT.emoji()} Pinpoint #123 4/5")
    }

    @Test
    fun `maps pinpoint fail`() {
        val failResult = pinpointResult.copy(score = 6)

        val shareLine = ShareLineMapper().mapToShareLine(failResult)

        assertThat(shareLine).isEqualTo("${Game.PINPOINT.emoji()} Pinpoint #123 X/5")
    }

    private val geocirclesResult = PuzzleResult(
        id = 1,
        userId = 1,
        game = Game.GEOCIRCLES,
        puzzleNumber = 123,
        puzzleDate = null,
        instantSubmitted = Instant.now(),
        score = 10,
        shareText = "share text",
        resultInfo = GeocirclesInfo,
    )

    @Test
    fun `maps geocircles line perfect`() {
        val shareLine = ShareLineMapper().mapToShareLine(geocirclesResult)

        assertThat(shareLine).isEqualTo("${Game.GEOCIRCLES.emoji()} Geocircles #123 5/5 \uD83C\uDFAF")
    }

    @Test
    fun `maps geocircles line lives left`() {
        val result = geocirclesResult.copy(score = 8)

        val shareLine = ShareLineMapper().mapToShareLine(result)

        assertThat(shareLine).isEqualTo("${Game.GEOCIRCLES.emoji()} Geocircles #123 5/5 (2 wrong)")
    }

    @Test
    fun `maps geocircles line some right`() {
        val result = geocirclesResult.copy(score = 4)

        val shareLine = ShareLineMapper().mapToShareLine(result)

        assertThat(shareLine).isEqualTo("${Game.GEOCIRCLES.emoji()} Geocircles #123 4/5")
    }

    @Test
    fun `maps geocircles line none right`() {
        val result = geocirclesResult.copy(score = 0)

        val shareLine = ShareLineMapper().mapToShareLine(result)

        assertThat(shareLine).isEqualTo("${Game.GEOCIRCLES.emoji()} Geocircles #123 0/5")
    }
}
