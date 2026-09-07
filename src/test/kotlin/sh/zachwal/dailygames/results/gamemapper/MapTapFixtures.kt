package sh.zachwal.dailygames.results.gamemapper

object MapTapFixtures {
    const val EXAMPLE = """
www.maptap.gg September 7
100🎯 93🏆 95🏅 66🤫 72🙃
Final score: 797
"""

    const val WITH_PERFECT_ROUND = """
www.maptap.gg May 4
100🎯 99🎯 78✨ 83😁 72🤗
Final score: 820
"""

    const val NO_PERFECT_ROUND = """
www.maptap.gg May 4
98🔥 99🎯 86🎓 98🎯 93🏆
Final score: 942
"""

    const val NO_WWW_PREFIX = """
maptap.gg September 6
100🎯 100🎯 100🎯 100🎯 98🔥
Final score: 994
"""

    val ALL = listOf(EXAMPLE, WITH_PERFECT_ROUND, NO_PERFECT_ROUND, NO_WWW_PREFIX)
}
