package sh.zachwal.dailygames.results.gamemapper

import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.Test
import sh.zachwal.dailygames.db.jdbi.User
import sh.zachwal.dailygames.db.jdbi.puzzle.Game
import sh.zachwal.dailygames.db.jdbi.puzzle.PuzzleResult
import sh.zachwal.dailygames.results.resultinfo.BracketCityInfo
import java.time.Instant
import java.time.LocalDate

class BracketCityMapperTest : GameMapperContractTest() {
    override val mapper = BracketCityMapper()
    override val fixtures = BracketCityFixtures.ALL
    private val testUser = User(id = 1L, username = "test", hashedPassword = "hash")

    private val bracketCityResult = PuzzleResult(
        id = 1,
        userId = 1,
        game = Game.BRACKET_CITY,
        puzzleNumber = 20250420,
        puzzleDate = LocalDate.of(2025, 4, 20),
        instantSubmitted = Instant.now(),
        score = 98,
        shareText = "share text",
        resultInfo = BracketCityInfo(
            rank = "Power Broker",
            rankEmoji = "💼",
            wrongGuesses = 1,
            peeks = 0,
            answersRevealed = 0,
            totalScore = 98.0,
            grid = "🟩🟩🟩🟩🟩🟩🟩🟩🟩🟩",
        ),
    )

    @Test
    fun `extracts bracket city power broker`() {
        val parsed = mapper.extract(BracketCityFixtures.POWER_BROKER, testUser).info<BracketCityInfo>()
        assertThat(parsed.rank).isEqualTo("Power Broker")
        assertThat(parsed.rankEmoji).isEqualTo("💼")
        assertThat(parsed.wrongGuesses).isEqualTo(1)
        assertThat(parsed.totalScore).isEqualTo(98.0)
        assertThat(parsed.grid).isEqualTo("🟩🟩🟩🟩🟩🟩🟩🟩🟩🟩")
    }

    @Test
    fun `extracts bracket city chief of police`() {
        val parsed = mapper.extract(BracketCityFixtures.CHIEF_OF_POLICE, testUser).info<BracketCityInfo>()
        assertThat(parsed.rank).isEqualTo("Chief of Police")
        assertThat(parsed.rankEmoji).isEqualTo("👮")
        assertThat(parsed.wrongGuesses).isEqualTo(3)
        assertThat(parsed.peeks).isEqualTo(2)
        assertThat(parsed.answersRevealed).isEqualTo(1)
        assertThat(parsed.totalScore).isEqualTo(69.0)
    }

    @Test
    fun `extracts bracket city kingmaker`() {
        val parsed = mapper.extract(BracketCityFixtures.KINGMAKER, testUser).info<BracketCityInfo>()
        assertThat(parsed.rank).isEqualTo("Kingmaker")
        assertThat(parsed.rankEmoji).isEqualTo("👑")
        assertThat(parsed.totalScore).isEqualTo(100.0)
    }

    @Test
    fun `extracts bracket city tourist`() {
        val parsed = mapper.extract(BracketCityFixtures.TOURIST, testUser).info<BracketCityInfo>()
        assertThat(parsed.rank).isEqualTo("Tourist")
        assertThat(parsed.rankEmoji).isEqualTo("📸")
        assertThat(parsed.peeks).isEqualTo(16)
        assertThat(parsed.answersRevealed).isEqualTo(16)
        assertThat(parsed.totalScore).isEqualTo(0.0)
    }

    @Test
    fun `extracts bracket city puppetmaster`() {
        val parsed = mapper.extract(BracketCityFixtures.PUPPETMASTER, testUser).info<BracketCityInfo>()
        assertThat(parsed.rank).isEqualTo("Puppet Master")
        assertThat(parsed.rankEmoji).isEqualTo("🔮")
        assertThat(parsed.wrongGuesses).isEqualTo(0)
        assertThat(parsed.totalScore).isEqualTo(100.0)
    }

    @Test
    fun `parses date and converts to YYYYMMDD format with different dates`() {
        val example1 = """
        [Bracket City]
        March 15, 2023

        https://www.theatlantic.com/games/bracket-city/

        Rank: 👑 (Kingmaker)
        ❌ Wrong guesses: 0

        Total Score: 100.0
        🟩🟩🟩🟩🟩🟩🟩🟩🟩🟩
        """.trimIndent()

        val parsedExample1 = mapper.extract(example1, testUser)
        assertThat(parsedExample1.date).isEqualTo(LocalDate.of(2023, 3, 15))
        assertThat(parsedExample1.puzzleNumber).isEqualTo(20230315)
    }

    @Test
    fun `formats share text nicely`() {
        val result = mapper.extract(BracketCityFixtures.KINGMAKER, testUser)

        assertThat(result.shareTextNoLink).isEqualTo(
            """
            Rank: 👑 (Kingmaker)
            ❌ Wrong guesses: 0

            Total Score: 100.0
            🟩🟩🟩🟩🟩🟩🟩🟩🟩🟩
            """.trimIndent(),
        )
    }

    @Test
    fun `maps Bracket City Power Broker`() {
        val shareLine = mapper.shareLine(bracketCityResult)
        assertThat(shareLine).isEqualTo("🏙️ Bracket City 4/20 98.0 💼")
    }

    @Test
    fun `maps Bracket City Kingmaker`() {
        val result = bracketCityResult.copy(
            score = 100,
            resultInfo = bracketCityResult.info<BracketCityInfo>().copy(rankEmoji = "👑", totalScore = 100.0),
        )
        assertThat(mapper.shareLine(result)).isEqualTo("🏙️ Bracket City 4/20 100.0 👑")
    }
}
