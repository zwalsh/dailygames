package sh.zachwal.dailygames.results.gamemapper

import sh.zachwal.dailygames.db.jdbi.User
import sh.zachwal.dailygames.db.jdbi.puzzle.Game
import sh.zachwal.dailygames.db.jdbi.puzzle.PuzzleResult
import sh.zachwal.dailygames.results.resultinfo.ParsedResult
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class KrillionMapper @Inject constructor() : GameMapper {
    override val game = Game.KRILLION

    override fun matches(shareText: String) = shareText.trim().startsWith("Krillion #")

    override fun extract(shareText: String, user: User): ParsedResult {
        TODO()
    }

    override fun shareLine(result: PuzzleResult): String {
        TODO()
    }
}
