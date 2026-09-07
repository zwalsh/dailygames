package sh.zachwal.dailygames.results.gamemapper

object WorldleFixtures {
    const val FOUR_OF_SIX = """
#Worldle #934 (12.08.2024) 4/6 (100%)
🟩🟩🟩🟩🟨⬅️
🟩🟩🟩🟩🟨⬅️
🟩🟩🟩🟩🟨↗️
🟩🟩🟩🟩🟩🎉

https://worldle.teuteuf.fr
"""

    const val FAILED = """
#Worldle #934 (12.08.2024) X/6 (100%)
🟩🟩🟩🟩🟨⬅️
🟩🟩🟩🟩🟨⬅️
🟩🟩🟩🟩🟨↗️
🟩🟩🟩🟩🟩🎉

https://worldle.teuteuf.fr
"""

    val ALL = listOf(FOUR_OF_SIX, FAILED)
}
