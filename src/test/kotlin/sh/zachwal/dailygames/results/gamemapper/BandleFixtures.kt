package sh.zachwal.dailygames.results.gamemapper

object BandleFixtures {
    const val PERFECT = """
Bandle #941 1/6
🟩⬜⬜⬜⬜⬜
Found: 1/1 (100%)
#Bandle #Heardle #Wordle

https://bandle.app/
"""

    const val ZERO = """
Bandle #941 x/6
🟥🟥🟥🟥🟥🟥
Found: 0/1 (0%)
#Bandle #Heardle #Wordle

https://bandle.app/
"""

    const val FOUR = """
Bandle #941 4/6
🟨🟥🟨🟩⬜⬜
Found: 1/1 (100%)
#Bandle #Heardle #Wordle

https://bandle.app/
"""

    const val SKIP = """
Bandle #941 x/6
⬛⬛⬛⬛⬛⬛
Found: 0/1 (0%)
#Bandle #Heardle #Wordle

https://bandle.app/
"""

    val ALL = listOf(PERFECT, ZERO, FOUR, SKIP)
}
