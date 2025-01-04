package sh.zachwal.dailygames.home

import javax.inject.Inject
import javax.inject.Singleton
import sh.zachwal.dailygames.db.dao.game.PuzzleResultDAO
import sh.zachwal.dailygames.db.jdbi.User

@Singleton
class StreakService @Inject constructor(
    private val resultDAO: PuzzleResultDAO,
) {

    fun streakForUser(user: User): Int {

        return 0
    }
}