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
)
