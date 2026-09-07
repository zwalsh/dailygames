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
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import sh.zachwal.dailygames.db.dao.game.PuzzleDAO
import sh.zachwal.dailygames.db.dao.game.PuzzleResultDAO
import sh.zachwal.dailygames.db.extension.DatabaseExtension
import sh.zachwal.dailygames.db.extension.Fixtures
import sh.zachwal.dailygames.db.jdbi.puzzle.Game
import sh.zachwal.dailygames.db.jdbi.puzzle.Puzzle
import sh.zachwal.dailygames.results.gamemapper.BandleFixtures
import sh.zachwal.dailygames.results.gamemapper.BracketCityFixtures
import sh.zachwal.dailygames.results.gamemapper.CardleFixtures
import sh.zachwal.dailygames.results.gamemapper.FlagleFixtures
import sh.zachwal.dailygames.results.gamemapper.FramedFixtures
import sh.zachwal.dailygames.results.gamemapper.GeoGridFixtures
import sh.zachwal.dailygames.results.gamemapper.GeocirclesFixtures
import sh.zachwal.dailygames.results.gamemapper.KrillionFixtures
import sh.zachwal.dailygames.results.gamemapper.MapTapFixtures
import sh.zachwal.dailygames.results.gamemapper.PinpointFixtures
import sh.zachwal.dailygames.results.gamemapper.SizeItUpFixtures
import sh.zachwal.dailygames.results.gamemapper.Top5Fixtures
import sh.zachwal.dailygames.results.gamemapper.TravleFixtures
import sh.zachwal.dailygames.results.gamemapper.allGameMappers
import sh.zachwal.dailygames.results.resultinfo.CardleInfo
import sh.zachwal.dailygames.results.resultinfo.FramedInfo
import sh.zachwal.dailygames.results.resultinfo.KrillionInfo
import sh.zachwal.dailygames.results.resultinfo.MapTapInfo
import sh.zachwal.dailygames.results.resultinfo.Top5Info
import sh.zachwal.dailygames.results.resultinfo.TravleInfo
import sh.zachwal.dailygames.results.resultinfo.WorldleInfo
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
    private val fixtures: Fixtures,
) {

    private val puzzleDAO = jdbi.onDemand<PuzzleDAO>()
    private val resultDAO = spyk(jdbi.onDemand<PuzzleResultDAO>())
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
        resultDAO = resultDAO,
        shareTextParser = ShareTextParser(
            allGameMappers(clock = clock, userPreferencesService = userPreferencesService),
        ),
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

        assertThat(result.userId).isEqualTo(fixtures.zach.id)
        assertThat(result.game).isEqualTo(Game.WORLDLE)
        assertThat(result.puzzleNumber).isEqualTo(934)
        assertThat(result.score).isEqualTo(4)
        assertThat(result.shareText).isEqualTo(
            """
            #Worldle #934 (12.08.2024) 4/6 (100%)
            🟩🟩🟩🟩🟨⬅️
            🟩🟩🟩🟩🟨⬅️
            🟩🟩🟩🟩🟨↗️
            🟩🟩🟩🟩🟩🎉
            """.trimIndent(),
        )

        assertThat(result.resultInfo).isInstanceOf(WorldleInfo::class.java)
        val info = result.info<WorldleInfo>()

        assertThat(info.percentage).isEqualTo(100)
    }

    @Test
    fun `can create Tradle result`() {
        val result = resultService.createResult(fixtures.zach, tradle890)

        assertThat(result.userId).isEqualTo(fixtures.zach.id)
        assertThat(result.game).isEqualTo(Game.TRADLE)
        assertThat(result.puzzleNumber).isEqualTo(890)
        assertThat(result.score).isEqualTo(7)
        assertThat(result.shareText).isEqualTo(
            """
            #Tradle #890 X/6
            🟩🟩⬜⬜⬜
            🟩🟩🟩🟩⬜
            🟩🟩🟩🟩🟨
            🟩🟩🟩🟩🟨
            🟩🟩🟩🟩🟨
            🟩🟩🟩🟩🟨
            """.trimIndent(),
        )
    }

    @Test
    fun `can create Travle result`() {
        val result = resultService.createResult(fixtures.zach, TravleFixtures.WITH_HINT)

        assertThat(result.userId).isEqualTo(fixtures.zach.id)
        assertThat(result.game).isEqualTo(Game.TRAVLE)
        assertThat(result.puzzleNumber).isEqualTo(606)
        assertThat(result.score).isEqualTo(2)
        assertThat(result.shareText).isEqualTo(
            """
            #travle #606 +2 (1 hint)
            ✅✅🟩🟧🟧✅
            """.trimIndent(),
        )

        assertThat(result.resultInfo).isInstanceOf(TravleInfo::class.java)
        val info = result.info<TravleInfo>()

        assertThat(info.numGuesses).isEqualTo(6)
        assertThat(info.numIncorrect).isEqualTo(2)
        assertThat(info.numPerfect).isEqualTo(3)
        assertThat(info.numHints).isEqualTo(1)
    }

    @Test
    fun `can create Top5 result`() {
        val result = resultService.createResult(fixtures.zach, Top5Fixtures.WITH_MISSES)

        assertThat(result.userId).isEqualTo(fixtures.zach.id)
        assertThat(result.game).isEqualTo(Game.TOP5)
        assertThat(result.puzzleNumber).isEqualTo(171)
        assertThat(result.score).isEqualTo(3)
        assertThat(result.shareText).isEqualTo(
            """
            Top 5 #171
            ⬜🟧🟨⬜⬜🟩⬜⬜
            """.trimIndent(),
        )

        assertThat(result.resultInfo).isInstanceOf(Top5Info::class.java)
        val info = result.info<Top5Info>()

        assertThat(info.numGuesses).isEqualTo(8)
        assertThat(info.numCorrect).isEqualTo(3)
        assertThat(info.isPerfect).isFalse()
    }

    @Test
    fun `can create Flagle result`() {
        val result = resultService.createResult(fixtures.zach, FlagleFixtures.FAILED)

        assertThat(result.userId).isEqualTo(fixtures.zach.id)
        assertThat(result.game).isEqualTo(Game.FLAGLE)
        assertThat(result.puzzleNumber).isEqualTo(905)
        assertThat(result.score).isEqualTo(7)
        assertThat(result.shareText).isEqualTo(
            """
            #Flagle #905 (14.08.2024) X/6
            🟥🟥🟥
            🟥🟥🟥
            """.trimIndent(),
        )
    }

    @Test
    fun `can create Pinpoint result`() {
        val result = resultService.createResult(fixtures.zach, PinpointFixtures.THREE)

        assertThat(result.userId).isEqualTo(fixtures.zach.id)
        assertThat(result.game).isEqualTo(Game.PINPOINT)
        assertThat(result.puzzleNumber).isEqualTo(126)
        assertThat(result.score).isEqualTo(3)
        assertThat(result.shareText).isEqualTo(
            """
            Pinpoint #126
            🤔 🤔 📌 ⬜ ⬜ (3/5)
            """.trimIndent(),
        )
    }

    @Test
    fun `can create Geocircles result`() {
        val result = resultService.createResult(fixtures.zach, GeocirclesFixtures.PERFECT)

        assertThat(result.userId).isEqualTo(fixtures.zach.id)
        assertThat(result.game).isEqualTo(Game.GEOCIRCLES)
        assertThat(result.puzzleNumber).isEqualTo(55)
        assertThat(result.score).isEqualTo(10)
        assertThat(result.shareText).isEqualTo(
            """
            Geocircles #55
            🟢🟢🟢🟢🟢
            ❤️❤️❤️❤️❤️
            """.trimIndent(),
        )
    }

    @Test
    fun `can create Framed result`() {
        val result = resultService.createResult(fixtures.zach, FramedFixtures.FOUR)

        assertThat(result.userId).isEqualTo(fixtures.zach.id)
        assertThat(result.game).isEqualTo(Game.FRAMED)
        assertThat(result.puzzleNumber).isEqualTo(990)
        assertThat(result.score).isEqualTo(4)
        assertThat(result.shareText).isEqualTo(
            """
            Framed #990
            🎥 🟥 🟥 🟥 🟩 ⬛ ⬛
            """.trimIndent(),
        )
        assertThat(result.info<FramedInfo>()).isEqualTo(FramedInfo)
    }

    @Test
    fun `can create a GeoGrid result`() {
        val result = resultService.createResult(fixtures.zach, GeoGridFixtures.PERFECT)

        assertThat(result.userId).isEqualTo(fixtures.zach.id)
        assertThat(result.game).isEqualTo(Game.GEOGRID)
        assertThat(result.puzzleNumber).isEqualTo(280)
        assertThat(result.score).isEqualTo(9)
        assertThat(result.shareText).isEqualTo(
            """
            ✅ ✅ ✅
            ✅ ✅ ✅
            ✅ ✅ ✅
            Score: 123.3
            Rank: 3,618 / 11,718
            """.trimIndent(),
        )
    }

    @Test
    fun `can create a Bandle result`() {
        val result = resultService.createResult(fixtures.zach, BandleFixtures.PERFECT)

        assertThat(result.userId).isEqualTo(fixtures.zach.id)
        assertThat(result.game).isEqualTo(Game.BANDLE)
        assertThat(result.puzzleNumber).isEqualTo(941)
        assertThat(result.score).isEqualTo(1)
        assertThat(result.shareText).isEqualTo(
            """
            Bandle #941 1/6
            🟩⬜⬜⬜⬜⬜
            """.trimIndent(),
        )
    }

    @Test
    fun `can create a Bracket City result`() {
        val result = resultService.createResult(fixtures.zach, BracketCityFixtures.KINGMAKER)

        assertThat(result.userId).isEqualTo(fixtures.zach.id)
        assertThat(result.game).isEqualTo(Game.BRACKET_CITY)
        assertThat(result.puzzleNumber).isEqualTo(20250420)
        assertThat(result.score).isEqualTo(100)
        assertThat(result.shareText).isEqualTo(
            """
            Rank: 👑 (Kingmaker)
            ❌ Wrong guesses: 0
            
            Total Score: 100.0
            🟩🟩🟩🟩🟩🟩🟩🟩🟩🟩
            """.trimIndent(),
        )
    }

    @Test
    fun `can create a Size It Up result`() {
        val result = resultService.createResult(fixtures.zach, SizeItUpFixtures.THREE_HUNDRED)

        assertThat(result.userId).isEqualTo(fixtures.zach.id)
        assertThat(result.game).isEqualTo(Game.SIZE_IT_UP)
        assertThat(result.puzzleNumber).isEqualTo(20240911)
        assertThat(result.score).isEqualTo(300)
        assertThat(result.shareText).isEqualTo(
            """
            Size It Up
            Overall Score 300
            🟥🟥🟥🟥⬜⬜⬜⬜⬜⬜
            🟥🟥🟥🟥🟥🟥🟥🟥🟥🟥
            🟥🟥🟥🟥🟥🟥🟥⬜⬜⬜
            🟥🟥🟥🟥🟥⬜⬜⬜⬜⬜
            🟥🟥🟥🟥⬜⬜⬜⬜⬜⬜
            """.trimIndent(),
        )
    }

    @Test
    fun `can create a Size It Up result uses the user's time zone`() {
        every { userPreferencesService.getTimeZone(fixtures.zach.id) } returns ZoneId.of("America/Los_Angeles")

        val result = resultService.createResult(fixtures.zach, SizeItUpFixtures.THREE_HUNDRED)

        // Instant is 1am ET / 10pm PT on 9/10, so the puzzle number should still be 9/10 in PT
        assertThat(result.puzzleNumber).isEqualTo(20240910)
    }

    @Test
    fun `can create a Cardle result`() {
        val result = resultService.createResult(fixtures.zach, CardleFixtures.PERFECT)

        assertThat(result.userId).isEqualTo(fixtures.zach.id)
        assertThat(result.game).isEqualTo(Game.CARDLE)
        assertThat(result.puzzleNumber).isEqualTo(20240911)
        assertThat(result.score).isEqualTo(15)
        assertThat(result.shareText).isEqualTo(
            """
            Cardle 1/5
            Streak 1🔥
            Total Score 15
            🟢 🟢 🟢
            """.trimIndent(),
        )
        assertThat(result.resultInfo).isInstanceOf(CardleInfo::class.java)
        val info = result.info<CardleInfo>()

        assertThat(info.numGuesses).isEqualTo(1)
        assertThat(info.streak).isEqualTo(1)
    }

    @Test
    fun `can create a Krillion result`() {
        val result = resultService.createResult(fixtures.zach, KrillionFixtures.MID)

        assertThat(result.userId).isEqualTo(fixtures.zach.id)
        assertThat(result.game).isEqualTo(Game.KRILLION)
        assertThat(result.puzzleNumber).isEqualTo(54)
        assertThat(result.score).isEqualTo(5)
        assertThat(result.shareText).isEqualTo(
            """
            Krillion #54 🦐
            385

            🐟🐟🦑🏮🦑🦑🦑
            """.trimIndent(),
        )
        assertThat(result.resultInfo).isInstanceOf(KrillionInfo::class.java)
        val info = result.info<KrillionInfo>()

        assertThat(info.rawScore).isEqualTo(385)
        assertThat(info.schoolerCount).isEqualTo(2)
        assertThat(info.rareCount).isEqualTo(4)
        assertThat(info.deepCutCount).isEqualTo(1)
    }

    @Test
    fun `can create a MapTap result`() {
        val result = resultService.createResult(fixtures.zach, MapTapFixtures.EXAMPLE)

        assertThat(result.userId).isEqualTo(fixtures.zach.id)
        assertThat(result.game).isEqualTo(Game.MAPTAP)
        assertThat(result.puzzleNumber).isEqualTo(20240907)
        assertThat(result.score).isEqualTo(797)
        assertThat(result.shareText).isEqualTo(
            """
            www.maptap.gg September 7
            100🎯 93🏆 95🏅 66🤫 72🙃
            Final score: 797
            """.trimIndent(),
        )
        assertThat(result.resultInfo).isInstanceOf(MapTapInfo::class.java)
        val info = result.info<MapTapInfo>()

        assertThat(info.finalScore).isEqualTo(797)
        assertThat(info.roundScores).isEqualTo(listOf(100, 93, 95, 66, 72))
    }

    @Test
    fun `result feed result title for Cardle is Game name followed by short date`() {
        resultService.createResult(fixtures.zach, CardleFixtures.PERFECT)
        val item = resultService.resultFeed(1L).single()

        assertThat(item.resultTitle).isEqualTo("Cardle 9/11")
    }

    @Test
    fun `submitting a result twice throws a helpful error`() {
        resultService.createResult(fixtures.zach, worldle934)

        val e = assertThrows<ConflictingPuzzleResultException> {
            resultService.createResult(fixtures.zach, worldle934)
        }

        assertThat(e.message).isEqualTo("You have already submitted a result for puzzle Worldle #934")
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
    fun `result feed result title for Size It Up is Game name followed by short date`() {
        resultService.createResult(fixtures.zach, SizeItUpFixtures.THREE_HUNDRED)
        val item = resultService.resultFeed(1L).single()

        assertThat(item.resultTitle).isEqualTo("Size It Up 9/11")
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
            """.trimIndent(),
        )

        val feedTitles = resultService.resultFeed(1L).map { it.resultTitle }

        assertThat(feedTitles).containsExactly("Worldle #935", "Worldle #934").inOrder()
    }

    @Test
    fun `result feed returns capped number of results`() {
        repeat(FEED_SIZE + 5) {
            resultService.createResult(
                fixtures.zach,
                worldle934.replace("934", (934 + it).toString()),
            )
        }

        val feed = resultService.resultFeed(1L)

        assertThat(feed).hasSize(FEED_SIZE)
    }

    @Test
    fun `result feed includes all types of results, ordered by submission time`() {
        resultService.createResult(fixtures.zach, worldle934)
        resultService.createResult(fixtures.jackie, tradle890)
        resultService.createResult(fixtures.zach, TravleFixtures.PLUS_0)
        resultService.createResult(fixtures.zach, Top5Fixtures.WITH_MISSES)
        resultService.createResult(fixtures.zach, FlagleFixtures.FAILED)
        resultService.createResult(fixtures.zach, PinpointFixtures.THREE)
        resultService.createResult(fixtures.zach, GeocirclesFixtures.PERFECT)

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
            """.trimIndent(),
        )
        val differentPuzzleResult = resultService.createResult(fixtures.zach, tradle890)

        val results = resultService.allResultsForPuzzle(Puzzle(Game.WORLDLE, 934, null))

        assertThat(results).hasSize(2)
        assertThat(results.all { it.game == Game.WORLDLE && it.puzzleNumber == 934 }).isTrue()
        assertThat(results.any { it.userId == fixtures.zach.id }).isTrue()
        assertThat(results.any { it.userId == fixtures.jackie.id }).isTrue()
        assertThat(results.any { it.game == Game.FLAGLE }).isFalse()

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
    fun `anyResultsToday returns true when results exist for today`() {
        val user = fixtures.zach
        val startOfToday = clock.instant().atZone(ZoneId.of("America/New_York")).truncatedTo(ChronoUnit.DAYS).toInstant()

        puzzleDAO.insertPuzzle(Puzzle(Game.WORLDLE, 554, null))
        resultDAO.insertResultWithInstantSubmitted(
            userId = user.id,
            puzzle = Puzzle(Game.WORLDLE, 554, null),
            score = 10,
            shareText = "Test",
            resultInfo = WorldleInfo(
                percentage = 100,
            ),
            instantSubmitted = startOfToday.plusSeconds(3600),
        )

        val hasResults = resultService.anyResultsToday(user)

        assertThat(hasResults).isTrue()
    }

    @Test
    fun `anyResultsToday returns false when no results exist for today`() {
        val user = fixtures.zach
        val hasResults = resultService.anyResultsToday(user)

        assertThat(hasResults).isFalse()
    }

    @Test
    fun `anyResultsToday ignores results outside of user's timezone boundaries`() {
        val user = fixtures.zach
        val startOfToday = clock.instant().atZone(ZoneId.of("America/New_York")).truncatedTo(ChronoUnit.DAYS).toInstant()
        val endOfToday = startOfToday.plus(1, ChronoUnit.DAYS)

        puzzleDAO.insertPuzzle(Puzzle(Game.WORLDLE, 456, null))
        resultDAO.insertResultWithInstantSubmitted(
            userId = user.id,
            puzzle = Puzzle(Game.WORLDLE, 456, null),
            score = 10,
            shareText = "Test",
            resultInfo = WorldleInfo(
                percentage = 100,
            ),
            instantSubmitted = startOfToday.minusSeconds(3600),
        )

        puzzleDAO.insertPuzzle(Puzzle(Game.WORLDLE, 798, null))
        resultDAO.insertResultWithInstantSubmitted(
            userId = user.id,
            puzzle = Puzzle(Game.WORLDLE, 798, null),
            score = 15,
            shareText = "Test",
            resultInfo = WorldleInfo(
                percentage = 100,
            ),
            instantSubmitted = endOfToday.plusSeconds(3600),
        )

        val hasResults = resultService.anyResultsToday(user)

        assertThat(hasResults).isFalse()
    }

    @Test
    fun `inserts results in the new result table`() {
        resultService.createResult(fixtures.zach, Top5Fixtures.WITH_MISSES)

        val result = resultDAO.allResultsStream().toList().single()

        assertThat(result.userId).isEqualTo(fixtures.zach.id)
        assertThat(result.game).isEqualTo(Game.TOP5)
        assertThat(result.puzzleNumber).isEqualTo(171)
        val now = Instant.now()
        assertThat(result.instantSubmitted).isIn(Range.closed(now.minusSeconds(10), now))
        assertThat(result.puzzleDate).isNull()
        assertThat(result.score).isEqualTo(3)
        assertThat(result.shareText).isEqualTo(Top5Fixtures.WITH_MISSES.trimIndent())
        assertThat(result.resultInfo).isInstanceOf(Top5Info::class.java)
        val top5Info = result.info<Top5Info>()
        assertThat(top5Info.numGuesses).isEqualTo(8)
        assertThat(top5Info.numCorrect).isEqualTo(3)
        assertThat(top5Info.isPerfect).isFalse()
    }

    @Test
    fun `returns recent game counts excluding user id 1`() {
        resultService.createResult(fixtures.zach, Top5Fixtures.WITH_MISSES)
        resultService.createResult(fixtures.jackie, Top5Fixtures.WITH_MISSES)
        resultService.createResult(fixtures.jackie, FlagleFixtures.FAILED)

        val counts = resultService.resultCountByGame(
            since = Instant.now().minusSeconds(10),
            excludeUserId = fixtures.zach.id,
        )

        assertThat(counts).containsExactly(
            Game.TOP5,
            1,
            Game.FLAGLE,
            1,
        )
    }
}
