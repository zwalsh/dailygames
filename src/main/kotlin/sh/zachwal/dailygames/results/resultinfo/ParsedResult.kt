package sh.zachwal.dailygames.results.resultinfo

import sh.zachwal.dailygames.db.jdbi.puzzle.Game
import java.time.LocalDate

data class ParsedResult(
    val puzzleNumber: Int,
    val game: Game,
    val date: LocalDate?,
    val score: Int,
    val shareTextNoLink: String,
    val resultInfo: ResultInfo,
) {
    inline fun <reified T : ResultInfo> info(): T {
        if (resultInfo !is T) {
            throw ClassCastException("Attempted to use $resultInfo (of type ${resultInfo::class.simpleName}) as ResultInfo of type ${T::class.simpleName} for ParsedResult.")
        }

        return resultInfo
    }
}
