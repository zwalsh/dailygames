package sh.zachwal.dailygames.results

import sh.zachwal.dailygames.db.dao.WorldleDAO
import sh.zachwal.dailygames.db.jdbi.User
import sh.zachwal.dailygames.db.jdbi.puzzle.PuzzleResult
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ResultService @Inject constructor(
    private val worldleDAO: WorldleDAO
) {

    fun createResult(
        user: User,
        shareText: String
    ): PuzzleResult {
        // regex & parse share text
        // create puzzle if needed
        // create result
        // return result

        TODO()
    }
}