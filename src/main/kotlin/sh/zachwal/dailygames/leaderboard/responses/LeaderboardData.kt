package sh.zachwal.dailygames.leaderboard.responses

data class LeaderboardData constructor(
    val allTimePoints: ChartInfo,
    val allTimeGames: ChartInfo,
    val allTimeAverage: ChartInfo,
    val thirtyDaysPoints: ChartInfo,
    val thirtyDaysGames: ChartInfo,
    val thirtyDaysAverage: ChartInfo,
    val pointsHistogram: ChartInfo,
)
