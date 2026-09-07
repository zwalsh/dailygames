package sh.zachwal.dailygames.results.gamemapper

object FlagleFixtures {
    const val FAILED = """
#Flagle #905 (14.08.2024) X/6
🟥🟥🟥
🟥🟥🟥
https://www.flagle.io
"""

    const val ONE_GUESS = """
#Flagle #905 (14.08.2024) 2/6
🟥🟩🟩
🟩🟩🟩
https://www.flagle.io
"""

    val ALL = listOf(FAILED, ONE_GUESS)
}
