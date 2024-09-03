package sh.zachwal.dailygames.home

import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.Test
import sh.zachwal.dailygames.db.jdbi.puzzle.Game
import sh.zachwal.dailygames.db.jdbi.puzzle.TradleResult
import sh.zachwal.dailygames.db.jdbi.puzzle.TravleResult
import sh.zachwal.dailygames.db.jdbi.puzzle.WorldleResult
import java.time.Instant

class ShareLineMapperTest {

    private val worldleResult = WorldleResult(
        id = 1,
        userId = 1,
        game = Game.WORLDLE,
        puzzleNumber = 123,
        puzzleDate = null,
        instantSubmitted = Instant.now(),
        score = 1,
        shareText = "share text",
        scorePercentage = 100,
    )

    @Test
    fun `maps worldle line`() {
        val shareLine = ShareLineMapper().mapToShareLine(worldleResult)

        assertThat(shareLine).isEqualTo("${Game.WORLDLE.emoji()} Worldle #123 1/6")
    }

    @Test
    fun `for Worldle fail, includes percentage`() {
        // Given
        val failResult = worldleResult.copy(score = 7, scorePercentage = 50)

        // When
        val shareLine = ShareLineMapper().mapToShareLine(failResult)

        // Then
        assertThat(shareLine).isEqualTo("${Game.WORLDLE.emoji()} Worldle #123 X/6 (50%)")
    }

    private val tradleResult = TradleResult(
        id = 1,
        userId = 1,
        game = Game.TRADLE,
        puzzleNumber = 123,
        puzzleDate = null,
        instantSubmitted = Instant.now(),
        score = 1,
        shareText = "share text",
    )

    @Test
    fun `maps tradle line`() {
        val shareLine = ShareLineMapper().mapToShareLine(tradleResult)

        assertThat(shareLine).isEqualTo("${Game.TRADLE.emoji()} Tradle #123 1/6")
    }

    @Test
    fun `maps tradle fail`() {
        val failResult = tradleResult.copy(score = 7)

        val shareLine = ShareLineMapper().mapToShareLine(failResult)

        assertThat(shareLine).isEqualTo("${Game.TRADLE.emoji()} Tradle #123 X/6")
    }

    private val travleResult = TravleResult(
        id = 1L,
        userId = 1L,
        game = Game.TRAVLE,
        puzzleNumber = 123,
        puzzleDate = null,
        instantSubmitted = Instant.now(),
        score = 0,
        shareText = "share text",
        numGuesses = 6,
        numIncorrect = 0,
        numPerfect = 6,
        numHints = 0,
    )

    @Test
    fun `maps travle perfect`() {
        val shareLine = ShareLineMapper().mapToShareLine(travleResult)

        assertThat(shareLine).isEqualTo("${Game.TRAVLE.emoji()} Travle #123 +0 (Perfect)")
    }

    @Test
    fun `maps travle plus zero non-perfect`() {
        val result = travleResult.copy(score = 0, numPerfect = 5)

        val shareLine = ShareLineMapper().mapToShareLine(result)

        assertThat(shareLine).isEqualTo("${Game.TRAVLE.emoji()} Travle #123 +0")
    }

    @Test
    fun `maps travle plus one`() {
        val result = travleResult.copy(score = 1, numGuesses = 7, numPerfect = 6, numIncorrect = 1)

        val shareLine = ShareLineMapper().mapToShareLine(result)

        assertThat(shareLine).isEqualTo("${Game.TRAVLE.emoji()} Travle #123 +1")
    }

    @Test
    fun `maps travle with hints`() {
        val result = travleResult.copy(score = 1, numGuesses = 7, numPerfect = 6, numIncorrect = 1, numHints = 2)

        val shareLine = ShareLineMapper().mapToShareLine(result)

        assertThat(shareLine).isEqualTo("${Game.TRAVLE.emoji()} Travle #123 +1 (2 hints)")
    }

    @Test
    fun `maps travle with negative score`() {
        val result = travleResult.copy(score = -1, numGuesses = 10, numPerfect = 6, numIncorrect = 4)

        val shareLine = ShareLineMapper().mapToShareLine(result)

        assertThat(shareLine).isEqualTo("${Game.TRAVLE.emoji()} Travle #123 (1 away)")
    }

    @Test
    fun `maps travle with negative score and one hint`() {
        val result = travleResult.copy(score = -1, numHints = 1, numGuesses = 10, numPerfect = 6, numIncorrect = 4)

        val shareLine = ShareLineMapper().mapToShareLine(result)

        assertThat(shareLine).isEqualTo("${Game.TRAVLE.emoji()} Travle #123 (1 away) (1 hint)")
    }
}