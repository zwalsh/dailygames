package sh.zachwal.dailygames.results

import com.google.common.truth.Truth.assertThat
import io.mockk.every
import io.mockk.mockk
import org.jdbi.v3.core.Jdbi
import org.jdbi.v3.sqlobject.kotlin.onDemand
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import sh.zachwal.dailygames.db.dao.game.PuzzleDAO
import sh.zachwal.dailygames.db.extension.DatabaseExtension
import sh.zachwal.dailygames.db.extension.Fixtures
import sh.zachwal.dailygames.db.jdbi.puzzle.Game
import sh.zachwal.dailygames.db.jdbi.puzzle.TradleResult
import sh.zachwal.dailygames.db.jdbi.puzzle.TravleResult
import sh.zachwal.dailygames.db.jdbi.puzzle.WorldleResult
import sh.zachwal.dailygames.home.views.ResultFeedItemView
import sh.zachwal.dailygames.users.UserService
import java.time.LocalDate

private val worldle934 = """
            #Worldle #934 (12.08.2024) 4/6 (100%)
            🟩🟩🟩🟩🟨⬅️
            🟩🟩🟩🟩🟨⬅️
            🟩🟩🟩🟩🟨↗️
            🟩🟩🟩🟩🟩🎉

            https://worldle.teuteuf.fr
""".trimIndent()

private val tradle890 = """
    #Tradle #890 X/6
    🟩🟩⬜⬜⬜
    🟩🟩🟩🟩⬜
    🟩🟩🟩🟩🟨
    🟩🟩🟩🟩🟨
    🟩🟩🟩🟩🟨
    🟩🟩🟩🟩🟨
    https://oec.world/en/games/tradle
""".trimIndent()

@ExtendWith(DatabaseExtension::class)
class ResultServiceTest(
    jdbi: Jdbi,
    private val fixtures: Fixtures
) {

    private val puzzleDAO = jdbi.onDemand<PuzzleDAO>()
    private val userService: UserService = mockk()

    private val resultService = ResultService(
        jdbi = jdbi,
        puzzleDAO = puzzleDAO,
        worldleDAO = jdbi.onDemand(),
        tradleDAO = jdbi.onDemand(),
        travleDAO = jdbi.onDemand(),
        shareTextParser = ShareTextParser(),
        userService = userService
    )

    @BeforeEach
    fun setup() {
        every { userService.getUser(fixtures.zach.id) } returns fixtures.zach
        every { userService.getUser(fixtures.jackie.id) } returns fixtures.jackie
    }

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
    fun `can create Tradle result`() {
        val result = resultService.createResult(fixtures.zach, tradle890)

        assertThat(result).isInstanceOf(TradleResult::class.java)

        val tradleResult = result as TradleResult

        assertThat(tradleResult.userId).isEqualTo(fixtures.zach.id)
        assertThat(tradleResult.game).isEqualTo(Game.TRADLE)
        assertThat(tradleResult.puzzleNumber).isEqualTo(890)
        assertThat(tradleResult.score).isEqualTo(0)
        assertThat(tradleResult.shareText).isEqualTo(
            """
            #Tradle #890 X/6
            🟩🟩⬜⬜⬜
            🟩🟩🟩🟩⬜
            🟩🟩🟩🟩🟨
            🟩🟩🟩🟩🟨
            🟩🟩🟩🟩🟨
            🟩🟩🟩🟩🟨
            """.trimIndent()
        )
    }

    @Test
    fun `can create Travle result`() {
        val result = resultService.createResult(fixtures.zach, TRAVLE_WITH_HINT)

        assertThat(result).isInstanceOf(TravleResult::class.java)

        val travleResult = result as TravleResult

        assertThat(travleResult.userId).isEqualTo(fixtures.zach.id)
        assertThat(travleResult.game).isEqualTo(Game.TRAVLE)
        assertThat(travleResult.puzzleNumber).isEqualTo(606)
        assertThat(travleResult.score).isEqualTo(2)
        assertThat(travleResult.shareText).isEqualTo(
            """
            #travle #606 +2 (1 hint)
            ✅✅🟩🟧🟧✅
            """.trimIndent()
        )
        assertThat(travleResult.numGuesses).isEqualTo(6)
        assertThat(travleResult.numIncorrect).isEqualTo(2)
        assertThat(travleResult.numPerfect).isEqualTo(3)
        assertThat(travleResult.numHints).isEqualTo(1)
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

    @Test
    fun `result feed result title is Game name in camel case, followed by puzzle number`() {
        resultService.createResult(fixtures.zach, worldle934)
        val item = resultService.resultFeed().single()

        assertThat(item.resultTitle).isEqualTo("Worldle #934")
    }

    @Test
    fun `result feed username matches user's name`() {
        resultService.createResult(fixtures.zach, worldle934)
        val item = resultService.resultFeed().single()

        assertThat(item.username).isEqualTo(fixtures.zach.username)
    }

    @Test
    fun `result feed returns results in order by submission time`() {
        val result1 = resultService.createResult(fixtures.zach, worldle934)
        val result2 = resultService.createResult(
            fixtures.jackie,
            """     
            #Worldle #935 (12.08.2024) 2/6 (100%)
            🟩🟩🟩🟩🟨⬅️
            🟩🟩🟩🟩🟩🎉
            """.trimIndent()
        )

        val feed = resultService.resultFeed()

        assertThat(feed).containsExactly(
            ResultFeedItemView(
                username = fixtures.jackie.username,
                resultTitle = "Worldle #935",
                shareText = result2.shareText,
            ),
            ResultFeedItemView(
                username = fixtures.zach.username,
                resultTitle = "Worldle #934",
                shareText = result1.shareText,
            ),
        )
    }

    @Test
    fun `result feed returns max 20 results`() {
        repeat(25) {
            resultService.createResult(fixtures.zach, worldle934)
        }

        val feed = resultService.resultFeed()

        assertThat(feed).hasSize(20)
    }

    @Test
    fun `result feed includes worldle and tradle results, ordered by submission time`() {
        val worldleResult = resultService.createResult(fixtures.zach, worldle934)
        val tradleResult = resultService.createResult(fixtures.jackie, tradle890)

        val feed = resultService.resultFeed()

        assertThat(feed).containsExactly(
            ResultFeedItemView(
                username = fixtures.jackie.username,
                resultTitle = "Tradle #890",
                shareText = tradleResult.shareText,
            ),
            ResultFeedItemView(
                username = fixtures.zach.username,
                resultTitle = "Worldle #934",
                shareText = worldleResult.shareText,
            ),
        )
    }
}
