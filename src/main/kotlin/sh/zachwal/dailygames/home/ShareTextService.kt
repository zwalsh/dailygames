package sh.zachwal.dailygames.home

import sh.zachwal.dailygames.db.jdbi.User
import sh.zachwal.dailygames.db.jdbi.puzzle.PuzzleResult
import sh.zachwal.dailygames.home.views.ShareTextModalView
import sh.zachwal.dailygames.leaderboard.PointCalculator
import sh.zachwal.dailygames.results.ResultService
import javax.inject.Inject
import javax.inject.Singleton

const val STREAK_THRESHOLD = 3

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

        val lines = shareTextLines + listOfNotNull(
            streakLine(user),
            pointsLine(results),
        )

        return ShareTextModalView(lines)
    }

    private fun streakLine(user: User): String? {
        val streak = streakService.streakForUser(user.id)
        val streakLine = if (streak >= STREAK_THRESHOLD) {
            "Streak: $streak\uD83D\uDD25"
        } else {
            null
        }
        return streakLine
    }

    private fun pointsLine(results: List<PuzzleResult>): String {
        val points = results.sumOf { pointCalculator.calculatePoints(it) }
        val maxPoints = results.sumOf { pointCalculator.maxPoints(it) }

        val pointsLine = "Points: $points/$maxPoints"
        return pointsLine
    }
}
