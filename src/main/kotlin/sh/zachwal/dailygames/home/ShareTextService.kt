package sh.zachwal.dailygames.home

import sh.zachwal.dailygames.db.jdbi.User
import sh.zachwal.dailygames.home.views.ShareTextModalView
import sh.zachwal.dailygames.leaderboard.PointCalculator
import sh.zachwal.dailygames.results.ResultService
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ShareTextService @Inject constructor(
    private val resultService: ResultService,
    private val shareLineMapper: ShareLineMapper,
    private val pointCalculator: PointCalculator,
    private val streakService: StreakService,
) {

    fun shareTextModalView(user: User): ShareTextModalView? {
        val results = resultService.resultsForUserToday(user)
        if (results.isEmpty()) {
            return null
        }

        val shareTextLines = results
            .map(shareLineMapper::mapToShareLine)

        val points = results.sumOf { pointCalculator.calculatePoints(it) }
        val maxPoints = results.sumOf { pointCalculator.maxPoints(it) }

        val pointsLine = "Points: $points/$maxPoints"

        return ShareTextModalView(shareTextLines + pointsLine)
    }
}
