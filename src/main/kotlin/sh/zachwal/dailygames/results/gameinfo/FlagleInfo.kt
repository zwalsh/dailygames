package sh.zachwal.dailygames.results.gameinfo

import java.time.LocalDate

data class FlagleInfo constructor(
    val puzzleNumber: Int,
    val date: LocalDate,
    val score: Int,
    val shareTextNoLink: String,
)
