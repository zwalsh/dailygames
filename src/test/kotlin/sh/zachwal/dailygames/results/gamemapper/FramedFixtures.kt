package sh.zachwal.dailygames.results.gamemapper

object FramedFixtures {
    const val PERFECT = """
Framed #990
🎥 🟩 ⬛ ⬛ ⬛ ⬛ ⬛

https://framed.wtf
"""

    const val ZERO = """
Framed #990
🎥 🟥 🟥 🟥 🟥 🟥 🟥

https://framed.wtf
"""

    const val FOUR = """
Framed #990
🎥 🟥 🟥 🟥 🟩 ⬛ ⬛

https://framed.wtf
"""

    const val SIX = """
Framed #990
🎥 🟥 🟥 🟥 🟥 🟥 🟩

https://framed.wtf
"""

    val ALL = listOf(PERFECT, ZERO, FOUR, SIX)
}
