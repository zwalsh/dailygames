package sh.zachwal.dailygames.leaderboard

data class ChartInfo(
    val labels: List<String>,
    val dataPoints: List<Double>
)

data class LeaderboardData(
    val allTime: ChartInfo,
    val past30Days: ChartInfo
)
