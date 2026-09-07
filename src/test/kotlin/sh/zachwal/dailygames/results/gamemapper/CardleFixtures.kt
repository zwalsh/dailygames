package sh.zachwal.dailygames.results.gamemapper

object CardleFixtures {
    const val PERFECT = """
Cardle 1/5
Streak 1🔥
Total Score 15
🟢 🟢 🟢
https://www.playcardle.com
"""

    const val MID = """
Cardle 4/5
Streak 1🔥
Total Score 6
🔴 🔴 🔴
🟢 🔴 🔴
🟢 🟢 🔴
🟢 🟢 🟢
https://www.playcardle.com
"""

    const val LOW_SCORE_NO_STREAK = """
Cardle 5/5
Total Score 1
🔴 🔴 🟢
🔴 🔴 🟢
🔴 🔴 🟢
🔴 🔴 🟢
🔴 🔴 🟢
https://www.playcardle.com
"""

    const val FAILURE = """
Cardle 5/5

🔴 🔴 🔴
🔴 🔴 🔴
🔴 🔴 🔴
🔴 🔴 🔴
🔴 🔴 🔴
https://www.playcardle.com
"""

    val ALL = listOf(PERFECT, MID, LOW_SCORE_NO_STREAK, FAILURE)
}
