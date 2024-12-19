package sh.zachwal.dailygames.wrapped

import com.google.common.truth.Truth.assertThat
import io.mockk.every
import io.mockk.mockk
import org.jdbi.v3.core.Jdbi
import org.jdbi.v3.sqlobject.kotlin.attach
import org.junit.jupiter.api.Test
import sh.zachwal.dailygames.db.dao.game.PuzzleResultDAO
import sh.zachwal.dailygames.db.jdbi.puzzle.Game
import sh.zachwal.dailygames.db.jdbi.puzzle.PuzzleResult
import sh.zachwal.dailygames.results.resultinfo.WorldleInfo
import java.time.Instant
import java.util.stream.Stream

class WrappedServiceTest {

    private val result = PuzzleResult(
        id = 1L,
        userId = 1L,
        game = Game.WORLDLE,
        puzzleNumber = 1,
        instantSubmitted = Instant.now(),
        puzzleDate = null,
        score = 5,
        shareText = "",
        resultInfo = WorldleInfo(
            percentage = 100,
        )
    )
    private val resultDAO = mockk<PuzzleResultDAO>()
    private val jdbi = mockk<Jdbi> {
        every { open() } returns mockk(relaxed = true) {
            every { attach<PuzzleResultDAO>() } returns resultDAO
        }
    }

    private val service = WrappedService(
        jdbi = jdbi,
    )

    @Test
    fun `wrapped data includes games played for each user`() {
        every { resultDAO.allResultsBetweenStream(any(), any()) } returns Stream.of(
            result,
            result.copy(),
            result.copy(),
            result.copy(userId = 2),
            result.copy(userId = 2),
        )

        val wrappedData = service.generateWrappedData(2024)

        assertThat(wrappedData).hasSize(2)

        val userOne = wrappedData.single { it.userId == 1L }
        assertThat(userOne.totalGamesPlayed).isEqualTo(3)

        val userTwo = wrappedData.single { it.userId == 2L }
        assertThat(userTwo.totalGamesPlayed).isEqualTo(2)
    }
}
