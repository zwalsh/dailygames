package sh.zachwal.dailygames.results.gamemapper

import sh.zachwal.dailygames.db.jdbi.User
import sh.zachwal.dailygames.db.jdbi.puzzle.Game
import sh.zachwal.dailygames.db.jdbi.puzzle.PuzzleResult
import sh.zachwal.dailygames.results.resultinfo.ParsedResult

interface GameMapper {
    val game: Game
    fun matches(shareText: String): Boolean
    fun extract(shareText: String, user: User): ParsedResult
    fun shareLine(result: PuzzleResult): String
}
