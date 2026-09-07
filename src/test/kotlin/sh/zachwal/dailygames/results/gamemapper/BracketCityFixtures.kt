package sh.zachwal.dailygames.results.gamemapper

object BracketCityFixtures {
    const val POWER_BROKER = """
[Bracket City]
April 20, 2025

https://www.theatlantic.com/games/bracket-city/

Rank: 💼 (Power Broker)
❌ Wrong guesses: 1

Total Score: 98.0
🟩🟩🟩🟩🟩🟩🟩🟩🟩🟩
"""

    const val CHIEF_OF_POLICE = """
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

    const val KINGMAKER = """
[Bracket City]
April 20, 2025

https://www.theatlantic.com/games/bracket-city/

Rank: 👑 (Kingmaker)
❌ Wrong guesses: 0

Total Score: 100.0
🟩🟩🟩🟩🟩🟩🟩🟩🟩🟩
"""

    const val TOURIST = """
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

    const val PUPPETMASTER = """
[Bracket City]
April 20, 2025

https://www.theatlantic.com/games/bracket-city/

Rank: 🔮 (Puppet Master)
🎹 Total Keystrokes: 63
🎯 Minimum Required: 63

Total Score: 100.0
🟪🟪🟪🟪🟪🟪🟪🟪🟪🟪
"""

    val ALL = listOf(POWER_BROKER, CHIEF_OF_POLICE, KINGMAKER, TOURIST, PUPPETMASTER)
}
