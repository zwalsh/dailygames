package sh.zachwal.dailygames.results.gameinfo

import java.time.LocalDate

data class WorldleInfo constructor(
    val puzzleNumber: Int,
    val date: LocalDate,
    val score: Int,
    val percentage: Int,
    val shareTextNoLink: String,
) : GameInfo()
