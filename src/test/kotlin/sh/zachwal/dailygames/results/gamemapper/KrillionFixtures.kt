package sh.zachwal.dailygames.results.gamemapper

object KrillionFixtures {
    const val PERFECT = """
Krillion #54 🦐
700

🌟🌟🌟🌟🌟🌟🌟
"""

    const val MID = """
Krillion #54 🦐
385

🐟🐟🦑🏮🦑🦑🦑
"""

    const val ZERO = """
Krillion #54 🦐
0

⬛⬛⬛⬛⬛⬛⬛
"""

    const val WITH_LINK = """
Krillion #54 🦐
355

⬛🐟🦑🏮🦑🦑🦑

https://krillion.io
"""

    const val PARTIAL_WITH_KRILLION = """
Krillion #54 🦐
485

🐟🐟🦑🏮🦑🌟🌟
"""

    val ALL = listOf(PERFECT, MID, ZERO, WITH_LINK, PARTIAL_WITH_KRILLION)
}
