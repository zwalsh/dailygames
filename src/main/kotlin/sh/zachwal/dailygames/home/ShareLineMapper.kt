package sh.zachwal.dailygames.home

import sh.zachwal.dailygames.db.jdbi.puzzle.PuzzleResult
import sh.zachwal.dailygames.results.gamemapper.GameMapper
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ShareLineMapper @Inject constructor(
    private val mappers: Set<@JvmSuppressWildcards GameMapper>,
) {

    fun mapToShareLine(result: PuzzleResult): String =
        mappers.first { it.game == result.game }.shareLine(result)
}
