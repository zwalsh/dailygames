package sh.zachwal.dailygames.results.gamemapper

object PinpointFixtures {
    const val THREE = """
Pinpoint #126
🤔 🤔 📌 ⬜ ⬜ (3/5)
lnkd.in/pinpoint
"""

    const val FAIL = """
Pinpoint #123
🤔 🤔 🤔 🤔 🤔 (X/5)
lnkd.in/pinpoint.
"""

    const val NO_LINK = """
Pinpoint #126
🤔 🤔 📌 ⬜ ⬜ (3/5)
"""

    const val TEST = """
Pinpoint #126
📌 ⬜ ⬜ ⬜ ⬜ (1/5)
"""

    val ALL = listOf(THREE, FAIL, NO_LINK, TEST)
}
