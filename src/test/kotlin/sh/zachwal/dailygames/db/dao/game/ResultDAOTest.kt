package sh.zachwal.dailygames.db.dao.game

import com.google.common.collect.Range
import com.google.common.truth.Truth.assertThat
import org.jdbi.v3.core.Jdbi
import org.jdbi.v3.sqlobject.kotlin.onDemand
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import sh.zachwal.dailygames.db.extension.DatabaseExtension
import sh.zachwal.dailygames.db.extension.Fixtures
import sh.zachwal.dailygames.db.jdbi.Result
import sh.zachwal.dailygames.db.jdbi.puzzle.Game
import sh.zachwal.dailygames.db.jdbi.puzzle.Puzzle
import sh.zachwal.dailygames.results.FLAGLE
import sh.zachwal.dailygames.results.resultinfo.FlagleInfo
import sh.zachwal.dailygames.results.resultinfo.ResultInfo
import sh.zachwal.dailygames.results.resultinfo.WorldleInfo
import java.time.Instant
import kotlin.streams.toList

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
        insertResult(
            userId = fixtures.zach.id,
            puzzle = fixtures.worldle123Puzzle,
        )
        insertResult(
            userId = fixtures.jackie.id,
            puzzle = fixtures.worldle123Puzzle,
        )

        val results = resultDAO.resultsForPuzzle(fixtures.worldle123Puzzle)

        assertThat(results).hasSize(2)
        assertThat(results.map { it.userId }).containsExactly(fixtures.zach.id, fixtures.jackie.id)
    }

    @Test
    fun `resultsForPuzzle excludes results for other games, puzzle numbers`() {
        val worldle124 = Puzzle(Game.WORLDLE, 124, null)
        puzzleDAO.insertPuzzle(worldle124)

        insertResult(
            puzzle = fixtures.worldle123Puzzle,
        )
        insertResult(
            puzzle = fixtures.flagle123Puzzle,
            resultInfo = FlagleInfo,
        )
        insertResult(
            puzzle = worldle124,
        )

        val results = resultDAO.resultsForPuzzle(fixtures.worldle123Puzzle)

        assertThat(results).hasSize(1)
        assertThat(results.map { it.userId }).containsExactly(fixtures.zach.id)
    }

    @Test
    fun `allResultsStream() lists all results across puzzles`() {
        insertResult(
            userId = fixtures.zach.id,
            puzzle = fixtures.worldle123Puzzle,
        )
        insertResult(
            userId = fixtures.jackie.id,
            puzzle = fixtures.worldle123Puzzle,
        )
        insertResult(
            userId = fixtures.zach.id,
            puzzle = fixtures.flagle123Puzzle,
            resultInfo = FlagleInfo,
        )

        val results = resultDAO.allResultsStream().toList()

        assertThat(results).hasSize(3)
        assertThat(results.map { it.game }).containsExactly(Game.WORLDLE, Game.WORLDLE, Game.FLAGLE)
        assertThat(results.map { it.puzzleNumber }).containsExactly(123, 123, 123)
    }

    @Test
    fun `allResultsStream() lists all results in descending order of submission`() {
        val resultOne = insertResult(
            userId = fixtures.zach.id,
            puzzle = fixtures.worldle123Puzzle,
        )
        val resultTwo = insertResult(
            userId = fixtures.jackie.id,
            puzzle = fixtures.worldle123Puzzle,
        )
        val resultThree = insertResult(
            userId = fixtures.zach.id,
            puzzle = fixtures.flagle123Puzzle,
            resultInfo = FlagleInfo,
        )

        val results = resultDAO.allResultsStream().toList()

        assertThat(results)
            .containsExactly(resultThree, resultTwo, resultOne)
            .inOrder()
    }

    private fun insertResult(
        userId: Long = fixtures.zach.id,
        puzzle: Puzzle = fixtures.worldle123Puzzle,
        score: Int = 5,
        shareText: String = "",
        resultInfo: ResultInfo = WorldleInfo(100),
    ): Result {
        return resultDAO.insertResult(userId, puzzle, score, shareText, resultInfo)
    }
}
