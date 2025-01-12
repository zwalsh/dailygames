package sh.zachwal.dailygames.leaderboard

data class ChartInfo(
    val labels: List<String>,
    val dataPoints: List<Double>
)

data class LeaderboardData constructor(
    val allTimePoints: ChartInfo,
    val allTimeGames: ChartInfo,
    val allTimeAverage: ChartInfo,
    val thirtyDaysPoints: ChartInfo,
    val thirtyDaysGames: ChartInfo,
    val thirtyDaysAverage: ChartInfo,
    val allTimeHistogram: ChartInfo,
)
