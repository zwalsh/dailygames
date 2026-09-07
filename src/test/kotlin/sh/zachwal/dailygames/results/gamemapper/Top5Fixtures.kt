package sh.zachwal.dailygames.results.gamemapper

object Top5Fixtures {
    const val WITH_MISSES = """
Top 5 #171
⬜🟧🟨⬜⬜🟩⬜⬜
"""

    const val ALL_5_WITH_MISSES = """
Top 5 #170
🟥⬜🟩🟨🟦⬜⬜⬜🟧
"""

    const val NO_MISSES = """
Top 5 #169
🟥🟩🟧🟦🟨
"""

    const val PERFECT = """
Top 5 #169
🟥🟧🟨🟩🟦
"""

    val ALL = listOf(WITH_MISSES, ALL_5_WITH_MISSES, NO_MISSES, PERFECT)
}
