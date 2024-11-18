package sh.zachwal.dailygames.results

import com.google.common.collect.Range
import com.google.common.truth.Truth.assertThat
import io.mockk.every
import io.mockk.mockk
import io.mockk.spyk
import io.mockk.verify
import org.jdbi.v3.core.Jdbi
import org.jdbi.v3.sqlobject.kotlin.onDemand
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.extension.ExtendWith
import sh.zachwal.dailygames.db.dao.game.PuzzleDAO
import sh.zachwal.dailygames.db.dao.game.ResultDAO
import sh.zachwal.dailygames.db.dao.game.WorldleDAO
import sh.zachwal.dailygames.db.extension.DatabaseExtension
import sh.zachwal.dailygames.db.extension.Fixtures
import sh.zachwal.dailygames.db.jdbi.puzzle.FlagleResult
import sh.zachwal.dailygames.db.jdbi.puzzle.Game
import sh.zachwal.dailygames.db.jdbi.puzzle.GeocirclesResult
import sh.zachwal.dailygames.db.jdbi.puzzle.PinpointResult
import sh.zachwal.dailygames.db.jdbi.puzzle.Puzzle
import sh.zachwal.dailygames.db.jdbi.puzzle.Top5Result
import sh.zachwal.dailygames.db.jdbi.puzzle.TradleResult
import sh.zachwal.dailygames.db.jdbi.puzzle.TravleResult
import sh.zachwal.dailygames.db.jdbi.puzzle.WorldleResult
import sh.zachwal.dailygames.results.resultinfo.Top5Info
import sh.zachwal.dailygames.users.UserPreferencesService
import sh.zachwal.dailygames.users.UserService
import sh.zachwal.dailygames.utils.DisplayTimeService
import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.temporal.ChronoUnit
import kotlin.streams.toList

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
    private val worldleDAO = spyk(jdbi.onDemand<WorldleDAO>())
    private val resultDAO = spyk(jdbi.onDemand<ResultDAO>())
    private val userService: UserService = mockk()

    private val displayTimeService = mockk<DisplayTimeService> {
        every { displayTime(any(), any(), any()) } returns "Just now"
        every { longDisplayTime(any(), any()) } returns "Long time ago"
    }
    private val userPreferencesService = mockk<UserPreferencesService> {
        every { getTimeZone(any()) } returns ZoneId.of("America/New_York")
    }
    private val instant = Instant.ofEpochSecond(1726030800) // 1am 9/11 ET, 10pm 9/10 PT
    private val clock = Clock.fixed(instant, ZoneId.of("America/New_York"))
    private val resultService = ResultService(
        jdbi = jdbi,
        puzzleDAO = puzzleDAO,
        worldleDAO = worldleDAO,
        tradleDAO = jdbi.onDemand(),
        travleDAO = jdbi.onDemand(),
        top5DAO = jdbi.onDemand(),
        flagleDAO = jdbi.onDemand(),
        pinpointDAO = jdbi.onDemand(),
        geocirclesDAO = jdbi.onDemand(),
        resultDAO = resultDAO,
        shareTextParser = ShareTextParser(),
        userService = userService,
        displayTimeService = displayTimeService,
        userPreferencesService = userPreferencesService,
        clock = clock,
    )

    @BeforeEach
    fun setup() {
        every { userService.getUser(fixtures.zach.id) } returns fixtures.zach
        every { userService.getUsernameCached(fixtures.zach.id) } returns fixtures.zach.username
        every { userService.getUser(fixtures.jackie.id) } returns fixtures.jackie
        every { userService.getUsernameCached(fixtures.jackie.id) } returns fixtures.jackie.username
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
        assertThat(tradleResult.score).isEqualTo(7)
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
    fun `can create Top5 result`() {
        val result = resultService.createResult(fixtures.zach, TOP5)

        assertThat(result).isInstanceOf(Top5Result::class.java)

        val top5Result = result as Top5Result

        assertThat(top5Result.userId).isEqualTo(fixtures.zach.id)
        assertThat(top5Result.game).isEqualTo(Game.TOP5)
        assertThat(top5Result.puzzleNumber).isEqualTo(171)
        assertThat(top5Result.score).isEqualTo(3)
        assertThat(top5Result.shareText).isEqualTo(
            """
            Top 5 #171
            ⬜🟧🟨⬜⬜🟩⬜⬜
            """.trimIndent()
        )
        assertThat(top5Result.numGuesses).isEqualTo(8)
        assertThat(top5Result.numCorrect).isEqualTo(3)
        assertThat(top5Result.isPerfect).isFalse()
    }

    @Test
    fun `can create Flagle result`() {
        val result = resultService.createResult(fixtures.zach, FLAGLE)

        assertThat(result).isInstanceOf(FlagleResult::class.java)

        val flagleResult = result as FlagleResult

        assertThat(flagleResult.userId).isEqualTo(fixtures.zach.id)
        assertThat(flagleResult.game).isEqualTo(Game.FLAGLE)
        assertThat(flagleResult.puzzleNumber).isEqualTo(905)
        assertThat(flagleResult.score).isEqualTo(7)
        assertThat(flagleResult.shareText).isEqualTo(
            """
            #Flagle #905 (14.08.2024) X/6
            🟥🟥🟥
            🟥🟥🟥
            """.trimIndent()
        )
    }

    @Test
    fun `can create Pinpoint result`() {
        val result = resultService.createResult(fixtures.zach, PINPOINT_THREE)

        assertThat(result).isInstanceOf(PinpointResult::class.java)

        val pinpointResult = result as PinpointResult

        assertThat(pinpointResult.userId).isEqualTo(fixtures.zach.id)
        assertThat(pinpointResult.game).isEqualTo(Game.PINPOINT)
        assertThat(pinpointResult.puzzleNumber).isEqualTo(126)
        assertThat(pinpointResult.score).isEqualTo(3)
        assertThat(pinpointResult.shareText).isEqualTo(
            """
            Pinpoint #126
            🤔 🤔 📌 ⬜ ⬜ (3/5)
            """.trimIndent()
        )
    }

    @Test
    fun `can create Geocircles result`() {
        val result = resultService.createResult(fixtures.zach, GEOCIRCLES_PERFECT)

        assertThat(result).isInstanceOf(GeocirclesResult::class.java)

        val geocirclesResult = result as GeocirclesResult

        assertThat(geocirclesResult.userId).isEqualTo(fixtures.zach.id)
        assertThat(geocirclesResult.game).isEqualTo(Game.GEOCIRCLES)
        assertThat(geocirclesResult.puzzleNumber).isEqualTo(55)
        assertThat(geocirclesResult.score).isEqualTo(10)
        assertThat(geocirclesResult.shareText).isEqualTo(
            """
            Geocircles #55
            🟢🟢🟢🟢🟢
            ❤️❤️❤️❤️❤️
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

    @Test
    fun `result feed result title is Game name in camel case, followed by puzzle number`() {
        resultService.createResult(fixtures.zach, worldle934)
        val item = resultService.resultFeed(1L).single()

        assertThat(item.resultTitle).isEqualTo("Worldle #934")
    }

    @Test
    fun `result feed username matches user's name`() {
        resultService.createResult(fixtures.zach, worldle934)
        val item = resultService.resultFeed(1L).single()

        assertThat(item.username).isEqualTo(fixtures.zach.username)
    }

    @Test
    fun `result feed item chat href is link to game puzzle`() {
        resultService.createResult(fixtures.zach, worldle934)
        val item = resultService.resultFeed(1L).single()

        assertThat(item.chatHref).isEqualTo("/game/worldle/puzzle/934")
    }

    @Test
    fun `result feed returns results in order by submission time`() {
        resultService.createResult(fixtures.zach, worldle934)
        resultService.createResult(
            fixtures.jackie,
            """     
            #Worldle #935 (12.08.2024) 2/6 (100%)
            🟩🟩🟩🟩🟨⬅️
            🟩🟩🟩🟩🟩🎉
            """.trimIndent()
        )

        val feedTitles = resultService.resultFeed(1L).map { it.resultTitle }

        assertThat(feedTitles).containsExactly("Worldle #935", "Worldle #934").inOrder()
    }

    @Test
    fun `result feed returns capped number of results`() {
        repeat(FEED_SIZE + 5) {
            resultService.createResult(fixtures.zach, worldle934)
        }

        val feed = resultService.resultFeed(1L)

        assertThat(feed).hasSize(FEED_SIZE)
    }

    @Test
    fun `result feed includes all types of results, ordered by submission time`() {
        resultService.createResult(fixtures.zach, worldle934)
        resultService.createResult(fixtures.jackie, tradle890)
        resultService.createResult(fixtures.zach, TRAVLE_PLUS_0)
        resultService.createResult(fixtures.zach, TOP5)
        resultService.createResult(fixtures.zach, FLAGLE)
        resultService.createResult(fixtures.zach, PINPOINT_THREE)
        resultService.createResult(fixtures.zach, GEOCIRCLES_PERFECT)

        val feedTitles = resultService.resultFeed(1L).map { it.resultTitle }

        assertThat(feedTitles).containsExactly(
            "Geocircles #55",
            "Pinpoint #126",
            "Flagle #905",
            "Top 5 #171",
            "Travle #607",
            "Tradle #890",
            "Worldle #934",
        ).inOrder()
    }

    @Test
    fun `can return all results for a puzzle`() {
        val result1 = resultService.createResult(fixtures.zach, worldle934)
        val result2 = resultService.createResult(
            fixtures.jackie,
            """     
            #Worldle #934 (12.08.2024) 2/6 (100%)
            🟩🟩🟩🟩🟨⬅️
            🟩🟩🟩🟩🟩🎉
            """.trimIndent()
        )
        val differentPuzzleResult = resultService.createResult(fixtures.zach, tradle890)

        val results = resultService.allResultsForPuzzle(Puzzle(Game.WORLDLE, 934, null))

        assertThat(results).containsExactly(result1, result2)
        assertThat(results).doesNotContain(differentPuzzleResult)
    }

    @Test
    fun `resultsForUserToday uses user's time zone`() {
        every { userPreferencesService.getTimeZone(fixtures.zach.id) } returns ZoneId.of("America/Los_Angeles")

        resultService.resultsForUserToday(fixtures.zach)

        // 12am PT / 3am ET on 9/10
        val expectedStart = Instant.ofEpochSecond(1725951600)
        // 12am PT + 1 day
        val expectedEnd = expectedStart.plus(1, ChronoUnit.DAYS)

        verify { resultDAO.resultsForUserInTimeRange(fixtures.zach.id, expectedStart, expectedEnd) }
    }

    @Test
    fun `inserts results in the new result table`() {
        resultService.createResult(fixtures.zach, TOP5)

        val result = resultDAO.allResultsStream().toList().single()

        assertThat(result.userId).isEqualTo(fixtures.zach.id)
        assertThat(result.game).isEqualTo(Game.TOP5)
        assertThat(result.puzzleNumber).isEqualTo(171)
        val now = Instant.now()
        assertThat(result.instantSubmitted).isIn(Range.closed(now.minusSeconds(10), now))
        assertThat(result.puzzleDate).isNull()
        assertThat(result.score).isEqualTo(3)
        assertThat(result.shareText).isEqualTo(TOP5.trimIndent())
        assertThat(result.resultInfo).isInstanceOf(Top5Info::class.java)
        val top5Info = result.resultInfo as Top5Info
        assertThat(top5Info.numGuesses).isEqualTo(8)
        assertThat(top5Info.numCorrect).isEqualTo(3)
        assertThat(top5Info.isPerfect).isFalse()
    }

    @Test
    fun `swallows exceptions when inserting into the result table`() {
        every { resultDAO.insertResult(any(), any(), any(), any(), any()) } throws RuntimeException("Database is down")

        assertDoesNotThrow {
            resultService.createResult(fixtures.zach, TOP5)
        }
    }
}
