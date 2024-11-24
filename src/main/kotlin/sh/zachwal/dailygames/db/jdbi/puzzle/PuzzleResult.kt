package sh.zachwal.dailygames.db.jdbi.puzzle

import org.jdbi.v3.json.Json
import sh.zachwal.dailygames.results.resultinfo.ResultInfo
import java.time.Instant
import java.time.LocalDate

data class PuzzleResult(
    val id: Long,
    val userId: Long,
    val game: Game,
    val puzzleNumber: Int,
    val instantSubmitted: Instant,
    val puzzleDate: LocalDate?,
    val score: Int,
    val shareText: String,
    @Json
    val resultInfo: ResultInfo,
) {
    inline fun <reified T : ResultInfo> info(): T {
        if (resultInfo !is T) {
            throw ClassCastException("Attempted to use $resultInfo (of type ${resultInfo::class.simpleName}) as ResultInfo of type ${T::class.simpleName} for result with id=$id")
        }

        return resultInfo
    }
}
