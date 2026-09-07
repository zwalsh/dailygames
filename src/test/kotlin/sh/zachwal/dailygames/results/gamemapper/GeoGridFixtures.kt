package sh.zachwal.dailygames.results.gamemapper

object GeoGridFixtures {
    const val PERFECT = """
✅ ✅ ✅
✅ ✅ ✅
✅ ✅ ✅

🌎Game Summary🌎
Board #280
Score: 123.3
Rank: 3,618 / 11,718
https://geogridgame.com
@geogridgame
"""

    const val ZERO = """
❌ ❌ ❌
❌ ❌ ❌
❌ ❌ ❌

🌎Game Summary🌎
Board #280
Score: 900
Rank: 10,188 / 11,737
https://geogridgame.com
@geogridgame
"""

    const val SIX = """
✅ ✅ ✅
❌ ❌ ✅
✅ ✅ ❌

🌎Game Summary🌎
Board #280
Score: 382.7
Rank: 9,311 / 11,761
https://geogridgame.com
@geogridgame
"""

    const val INFINITE = """
✅ ✅ ✅
✅ ✅ ✅
✅ ✅ ✅

🌎Game Summary🌎
Board #280
Score: 88.9
Rank: 1,521 / 11,795
https://geogridgame.com
@geogridgame
"""

    val ALL = listOf(PERFECT, ZERO, SIX, INFINITE)
}
