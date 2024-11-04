package sh.zachwal.dailygames.db.dao.game

import com.google.common.collect.Range
import com.google.common.truth.Truth.assertThat
import org.jdbi.v3.core.Jdbi
import org.jdbi.v3.sqlobject.kotlin.onDemand
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import sh.zachwal.dailygames.db.extension.DatabaseExtension
import sh.zachwal.dailygames.db.extension.Fixtures
import sh.zachwal.dailygames.db.jdbi.puzzle.Game
import sh.zachwal.dailygames.db.jdbi.puzzle.Puzzle
import sh.zachwal.dailygames.results.resultinfo.FlagleInfo
import sh.zachwal.dailygames.results.resultinfo.WorldleInfo
import java.time.Instant

@ExtendWith(DatabaseExtension::class)
class ResultDAOTest(
    jdbi: Jdbi,
    private val fixtures: Fixtures
) {

    private val resultDAO: ResultDAO = jdbi.onDemand()
    private val puzzleDAO: PuzzleDAO = jdbi.onDemand()

    @Test
    fun `can insert a result`() {
        val expectedWorldleInfo = WorldleInfo(
            3,
        )
        val result = resultDAO.insertResult(
            userId = fixtures.zach.id,
            puzzle = fixtures.worldle123Puzzle,
            score = 3,
            shareText = "Worldle #123 2.11.2024 3/6",
            resultInfo = expectedWorldleInfo
        )

        assertThat(result.userId).isEqualTo(fixtures.zach.id)
        assertThat(result.game).isEqualTo(Game.WORLDLE)
        assertThat(result.puzzleNumber).isEqualTo(123)
        assertThat(result.instantSubmitted).isIn(Range.closed(Instant.now().minusSeconds(10), Instant.now()))
        assertThat(result.score).isEqualTo(3)
        assertThat(result.shareText).isEqualTo("Worldle #123 2.11.2024 3/6")
        assertThat(result.resultInfo).isInstanceOf(WorldleInfo::class.java)
        val worldleInfo = result.resultInfo as WorldleInfo
        assertThat(worldleInfo).isEqualTo(expectedWorldleInfo)
    }

    @Test
    fun `can query results by puzzle`() {
        resultDAO.insertResult(
            userId = fixtures.zach.id,
            puzzle = fixtures.worldle123Puzzle,
            score = 3,
            shareText = "Worldle #123",
            resultInfo = WorldleInfo(100),
        )
        resultDAO.insertResult(
            userId = fixtures.jackie.id,
            puzzle = fixtures.worldle123Puzzle,
            score = 4,
            shareText = "Worldle #123",
            resultInfo = WorldleInfo(100),
        )

        val results = resultDAO.resultsForPuzzle(fixtures.worldle123Puzzle)

        assertThat(results).hasSize(2)
        assertThat(results.map { it.userId }).containsExactly(fixtures.zach.id, fixtures.jackie.id)
    }

    @Test
    fun `resultsForPuzzle excludes results for other games, puzzle numbers`() {
        resultDAO.insertResult(
            userId = fixtures.zach.id,
            puzzle = fixtures.worldle123Puzzle,
            score = 3,
            shareText = "Worldle #123",
            resultInfo = WorldleInfo(100),
        )
        resultDAO.insertResult(
            userId = fixtures.zach.id,
            puzzle = fixtures.flagle123Puzzle,
            score = 3,
            shareText = "Flagle #123",
            resultInfo = FlagleInfo,
        )
        val worldle124 = Puzzle(Game.WORLDLE, 124, null)
        puzzleDAO.insertPuzzle(worldle124)
        resultDAO.insertResult(
            userId = fixtures.jackie.id,
            puzzle = worldle124,
            score = 4,
            shareText = "Worldle #124",
            resultInfo = WorldleInfo(100),
        )

        val results = resultDAO.resultsForPuzzle(fixtures.worldle123Puzzle)

        assertThat(results).hasSize(1)
        assertThat(results.map { it.userId }).containsExactly(fixtures.zach.id)
    }
}
