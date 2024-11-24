package sh.zachwal.dailygames.db.jdbi.puzzle

import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import sh.zachwal.dailygames.results.resultinfo.FlagleInfo
import sh.zachwal.dailygames.results.resultinfo.WorldleInfo
import java.time.Instant

class PuzzleResultTest {

    private val worldleResult = PuzzleResult(
        id = 1L,
        userId = 1L,
        game = Game.WORLDLE,
        puzzleNumber = 30,
        instantSubmitted = Instant.now(),
        puzzleDate = null,
        score = 5,
        shareText = "",
        resultInfo = WorldleInfo(
            percentage = 100
        ),
    )

    @Test
    fun `can cast resultInfo to specific type`() {
        val worldleInfo = worldleResult.info<WorldleInfo>()

        assertThat(worldleInfo).isInstanceOf(WorldleInfo::class.java)
    }

    @Test
    fun `throws helpful ClassCastException when trying to coerce resultInfo to incorrect type`() {
        val exception = assertThrows<ClassCastException> {
            worldleResult.info<FlagleInfo>()
        }

        assertThat(exception).hasMessageThat().contains("Attempted to use")
        assertThat(exception).hasMessageThat().contains("as ResultInfo of type FlagleInfo")
        assertThat(exception).hasMessageThat().contains("for result with id=1")
    }
}