package sh.zachwal.dailygames.home

import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.Test
import sh.zachwal.dailygames.db.jdbi.puzzle.Game
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
}