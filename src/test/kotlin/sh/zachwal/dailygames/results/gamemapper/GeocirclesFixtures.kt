package sh.zachwal.dailygames.results.gamemapper

object GeocirclesFixtures {
    const val PERFECT = """
Geocircles #55
🟢🟢🟢🟢🟢
❤️❤️❤️❤️❤️
https://geocircles.io/55
"""

    const val ZERO_POINTS = """
Geocircles #55
⚫⚫⚫⚫⚫
🖤🖤🖤🖤🖤
https://geocircles.io/55
"""

    const val DNF = """
Geocircles #55
🟢🟢🟢🟢⚫
🖤🖤🖤🖤🖤
https://geocircles.io/55
"""

    const val LIVES_LEFT = """
Geocircles #55
🟢🟢🟢🟢🟢
❤️❤️🖤🖤🖤
https://geocircles.io/55
"""

    val ALL = listOf(PERFECT, ZERO_POINTS, DNF, LIVES_LEFT)
}
