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

    const val TITLED_PREVIEW = """
Top 5 Email domains that receive the most emails (#960)
⬜🟩⬜🟦⬜⬜⬜
https://deploy-preview-135--top5-game.netlify.app/?utm_medium=share
"""

    const val TITLED = """
Top 5 Email domains that receive the most emails (#960)
🟥🟩🟦🟨⬜⬜⬜⬜🟧
https://topfivetrivia.com/?utm_medium=share
"""

    const val TITLED_NO_LINK = """
Top 5 Email domains that receive the most emails (#960)
🟥🟩🟦🟨⬜⬜⬜⬜🟧
"""

    const val TITLED_PERFECT = """
Top 5 Best selling albums of 1999 (#961)
🟥🟧🟨🟩🟦
https://topfivetrivia.com/?utm_medium=share
"""

    val ALL = listOf(
        WITH_MISSES,
        ALL_5_WITH_MISSES,
        NO_MISSES,
        PERFECT,
        TITLED_PREVIEW,
        TITLED,
        TITLED_NO_LINK,
        TITLED_PERFECT,
    )
}
