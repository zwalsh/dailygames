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
import sh.zachwal.dailygames.results.gameinfo.WorldleInfo
import java.time.Instant
import java.time.LocalDate

@ExtendWith(DatabaseExtension::class)
class ResultDAOTest(
    jdbi: Jdbi,
    private val fixtures: Fixtures
) {

    val resultDAO: ResultDAO = jdbi.onDemand()

    @Test
    fun `can insert a result`() {
        val expectedWorldleInfo = WorldleInfo(
            fixtures.worldle123Puzzle.number,
            LocalDate.of(2024, 11, 2),
            3,
            100,
            "text",
        )
        val result = resultDAO.insertResult(
            userId = fixtures.zach.id,
            puzzle = fixtures.worldle123Puzzle,
            score = 3,
            shareText = "",
            resultInfo = expectedWorldleInfo
        )

        assertThat(result.userId).isEqualTo(fixtures.zach.id)
        assertThat(result.game).isEqualTo(Game.WORLDLE)
        assertThat(result.puzzleNumber).isEqualTo(123)
        assertThat(result.instantSubmitted).isIn(Range.closed(Instant.now().minusSeconds(10), Instant.now()))
        assertThat(result.score).isEqualTo(3)
        assertThat(result.shareText).isEqualTo("")
        assertThat(result.resultInfo).isInstanceOf(WorldleInfo::class.java)
        val worldleInfo = result.resultInfo as WorldleInfo
        assertThat(worldleInfo).isEqualTo(expectedWorldleInfo)
    }
}