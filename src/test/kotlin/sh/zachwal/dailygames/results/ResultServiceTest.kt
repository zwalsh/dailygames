package sh.zachwal.dailygames.results

import com.google.common.truth.Truth.assertThat
import org.jdbi.v3.core.Jdbi
import org.jdbi.v3.sqlobject.kotlin.onDemand
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import sh.zachwal.dailygames.db.dao.PuzzleDAO
import sh.zachwal.dailygames.db.extension.DatabaseExtension
import sh.zachwal.dailygames.db.extension.Fixtures
import sh.zachwal.dailygames.db.jdbi.puzzle.Game
import sh.zachwal.dailygames.db.jdbi.puzzle.WorldleResult
import java.time.LocalDate

private val worldle934 = """
            #Worldle #934 (12.08.2024) 4/6 (100%)
            🟩🟩🟩🟩🟨⬅️
            🟩🟩🟩🟩🟨⬅️
            🟩🟩🟩🟩🟨↗️
            🟩🟩🟩🟩🟩🎉

            https://worldle.teuteuf.fr
        """.trimIndent()

@ExtendWith(DatabaseExtension::class)
class ResultServiceTest(
    jdbi: Jdbi,
    private val fixtures: Fixtures
) {

    private val puzzleDAO = jdbi.onDemand<PuzzleDAO>()
    private val resultService = ResultService(
        puzzleDAO = puzzleDAO,
        worldleDAO = jdbi.onDemand(),
        shareTextParser = ShareTextParser()
    )

    @Test
    fun `can create worldle result`() {

        val result = resultService.createResult(fixtures.zach, worldle934)

        assertThat(result).isInstanceOf(WorldleResult::class.java)

        val worldleResult = result as WorldleResult

        assertThat(worldleResult.userId).isEqualTo(fixtures.zach.id)
        assertThat(worldleResult.game).isEqualTo(Game.WORLDLE)
        assertThat(worldleResult.puzzleNumber).isEqualTo(934)
        assertThat(worldleResult.score).isEqualTo(4)
        assertThat(worldleResult.scorePercentage).isEqualTo(100)
        assertThat(worldleResult.shareText).isEqualTo(
            """
            #Worldle #934 (12.08.2024) 4/6 (100%)
            🟩🟩🟩🟩🟨⬅️
            🟩🟩🟩🟩🟨⬅️
            🟩🟩🟩🟩🟨↗️
            🟩🟩🟩🟩🟩🎉
        """.trimIndent()
        )
    }

    @Test
    fun `creating a result creates a Puzzle record if necessary`() {
        resultService.createResult(fixtures.zach, worldle934)

        val puzzle = puzzleDAO.getPuzzle(Game.WORLDLE, 934)

        assertThat(puzzle).isNotNull()
        assertThat(puzzle!!.game).isEqualTo(Game.WORLDLE)
        assertThat(puzzle.number).isEqualTo(934)
        assertThat(puzzle.date).isEqualTo(LocalDate.of(2024, 8, 12))
    }
}