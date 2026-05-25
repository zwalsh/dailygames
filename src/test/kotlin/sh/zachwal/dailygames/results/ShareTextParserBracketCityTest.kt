package sh.zachwal.dailygames.results

import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.Test
import sh.zachwal.dailygames.db.jdbi.puzzle.Game
import sh.zachwal.dailygames.results.resultinfo.BracketCityInfo
import java.time.LocalDate

const val BRACKET_CITY_POWER_BROKER = """
[Bracket City]
April 20, 2025

https://www.theatlantic.com/games/bracket-city/

Rank: 💼 (Power Broker)
❌ Wrong guesses: 1

Total Score: 98.0
🟩🟩🟩🟩🟩🟩🟩🟩🟩🟩
"""

const val BRACKET_CITY_CHIEF_OF_POLICE = """
[Bracket City]
April 20, 2025

https://www.theatlantic.com/games/bracket-city/

Rank: 👮 (Chief of Police)
❌ Wrong guesses: 3
👀 Peeks: 2
🛟 Answers Revealed: 1

Total Score: 69.0
🟨🟨🟨🟨🟨🟨🟨⬜⬜⬜
"""

const val BRACKET_CITY_KINGMAKER = """
[Bracket City]
April 20, 2025

https://www.theatlantic.com/games/bracket-city/

Rank: 👑 (Kingmaker)
❌ Wrong guesses: 0

Total Score: 100.0
🟩🟩🟩🟩🟩🟩🟩🟩🟩🟩
"""

const val BRACKET_CITY_TOURIST = """
[Bracket City]
April 20, 2025

https://www.theatlantic.com/games/bracket-city/

Rank: 📸 (Tourist)
❌ Wrong guesses: 0
👀 Peeks: 16
🛟 Answers Revealed: 16

Total Score: 0.0
⬜⬜⬜⬜⬜⬜⬜⬜⬜⬜
"""

const val BRACKET_CITY_PUPPETMASTER = """
[Bracket City]
April 20, 2025

https://www.theatlantic.com/games/bracket-city/

Rank: 🔮 (Puppet Master)
🎹 Total Keystrokes: 63
🎯 Minimum Required: 63

Total Score: 100.0
🟪🟪🟪🟪🟪🟪🟪🟪🟪🟪
"""

class ShareTextParserBracketCityTest {
    private val parser = ShareTextParser()

    @Test
    fun `matches bracket city`() {
        assertThat(parser.identifyGame(BRACKET_CITY_POWER_BROKER)).isEqualTo(Game.BRACKET_CITY)
        assertThat(parser.identifyGame(BRACKET_CITY_CHIEF_OF_POLICE)).isEqualTo(Game.BRACKET_CITY)
        assertThat(parser.identifyGame(BRACKET_CITY_KINGMAKER)).isEqualTo(Game.BRACKET_CITY)
        assertThat(parser.identifyGame(BRACKET_CITY_TOURIST)).isEqualTo(Game.BRACKET_CITY)
    }

    @Test
    fun `extracts bracket city power broker`() {
        val parsed = parser.extractBracketCityInfo(BRACKET_CITY_POWER_BROKER).info<BracketCityInfo>()
        assertThat(parsed.rank).isEqualTo("Power Broker")
        assertThat(parsed.rankEmoji).isEqualTo("💼")
        assertThat(parsed.wrongGuesses).isEqualTo(1)
        assertThat(parsed.totalScore).isEqualTo(98.0)
        assertThat(parsed.grid).isEqualTo("🟩🟩🟩🟩🟩🟩🟩🟩🟩🟩")
    }

    @Test
    fun `extracts bracket city chief of police`() {
        val parsed = parser.extractBracketCityInfo(BRACKET_CITY_CHIEF_OF_POLICE).info<BracketCityInfo>()
        assertThat(parsed.rank).isEqualTo("Chief of Police")
        assertThat(parsed.rankEmoji).isEqualTo("👮")
        assertThat(parsed.wrongGuesses).isEqualTo(3)
        assertThat(parsed.peeks).isEqualTo(2)
        assertThat(parsed.answersRevealed).isEqualTo(1)
        assertThat(parsed.totalScore).isEqualTo(69.0)
        assertThat(parsed.grid).isEqualTo("🟨🟨🟨🟨🟨🟨🟨⬜⬜⬜")
    }

    @Test
    fun `extracts bracket city kingmaker`() {
        val parsed = parser.extractBracketCityInfo(BRACKET_CITY_KINGMAKER).info<BracketCityInfo>()
        assertThat(parsed.rank).isEqualTo("Kingmaker")
        assertThat(parsed.rankEmoji).isEqualTo("👑")
        assertThat(parsed.wrongGuesses).isEqualTo(0)
        assertThat(parsed.peeks).isEqualTo(0)
        assertThat(parsed.answersRevealed).isEqualTo(0)
        assertThat(parsed.totalScore).isEqualTo(100.0)
        assertThat(parsed.grid).isEqualTo("🟩🟩🟩🟩🟩🟩🟩🟩🟩🟩")
    }

    @Test
    fun `extracts bracket city tourist`() {
        val parsed = parser.extractBracketCityInfo(BRACKET_CITY_TOURIST).info<BracketCityInfo>()
        assertThat(parsed.rank).isEqualTo("Tourist")
        assertThat(parsed.rankEmoji).isEqualTo("📸")
        assertThat(parsed.wrongGuesses).isEqualTo(0)
        assertThat(parsed.peeks).isEqualTo(16)
        assertThat(parsed.answersRevealed).isEqualTo(16)
        assertThat(parsed.totalScore).isEqualTo(0.0)
        assertThat(parsed.grid).isEqualTo("⬜⬜⬜⬜⬜⬜⬜⬜⬜⬜")
    }

    @Test
    fun `extracts bracket city puppetmaster`() {
        val parsed = parser.extractBracketCityInfo(BRACKET_CITY_PUPPETMASTER).info<BracketCityInfo>()
        assertThat(parsed.rank).isEqualTo("Puppet Master")
        assertThat(parsed.rankEmoji).isEqualTo("🔮")
        assertThat(parsed.wrongGuesses).isEqualTo(0)
        assertThat(parsed.peeks).isEqualTo(0)
        assertThat(parsed.answersRevealed).isEqualTo(0)
        assertThat(parsed.totalScore).isEqualTo(100.0)
        assertThat(parsed.grid).isEqualTo("🟪🟪🟪🟪🟪🟪🟪🟪🟪🟪")
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

        val example2 = """
        [Bracket City]
        December 25, 2024

        https://www.theatlantic.com/games/bracket-city/

        Rank: 📸 (Tourist)
        ❌ Wrong guesses: 0
        👀 Peeks: 16
        🛟 Answers Revealed: 16
        
        Total Score: 100.0
        🟩🟩🟩🟩🟩🟩🟩🟩🟩🟩
        """.trimIndent()

        val parsedExample1 = parser.extractBracketCityInfo(example1)
        assertThat(parsedExample1.date).isEqualTo(LocalDate.of(2023, 3, 15))
        assertThat(parsedExample1.puzzleNumber).isEqualTo(20230315)

        val parsedExample2 = parser.extractBracketCityInfo(example2)
        assertThat(parsedExample2.date).isEqualTo(LocalDate.of(2024, 12, 25))
        assertThat(parsedExample2.puzzleNumber).isEqualTo(20241225)
    }

    @Test
    fun `formats share text nicely`() {
        val result = parser.extractBracketCityInfo(BRACKET_CITY_KINGMAKER)

        assertThat(result.shareTextNoLink).isEqualTo(
            """
            Rank: 👑 (Kingmaker)
            ❌ Wrong guesses: 0
            
            Total Score: 100.0
            🟩🟩🟩🟩🟩🟩🟩🟩🟩🟩
            """.trimIndent(),
        )
    }
}
