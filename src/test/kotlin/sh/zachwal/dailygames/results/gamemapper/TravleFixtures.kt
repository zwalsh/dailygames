package sh.zachwal.dailygames.results.gamemapper

object TravleFixtures {
    const val PERFECT = """
#travle #607 +0 (Perfect)
✅✅✅✅✅✅✅
https://travle.earth
"""

    const val PLUS_0 = """
#travle #607 +0
✅✅✅🟩✅✅✅
"""

    const val WITH_HINT = """
#travle #606 +2 (1 hint)
✅✅🟩🟧🟧✅
"""

    const val THREE_AWAY = """
#travle #614 (3 away)
🟧🟥🟥🟥🟧🟥🟥🟥✅
https://travle.earth
"""

    val ALL = listOf(PERFECT, PLUS_0, WITH_HINT, THREE_AWAY)
}
