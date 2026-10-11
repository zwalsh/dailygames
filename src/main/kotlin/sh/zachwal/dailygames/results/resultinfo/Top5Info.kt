package sh.zachwal.dailygames.results.resultinfo

data class Top5Info constructor(
    val numGuesses: Int,
    val numCorrect: Int,
    val isPerfect: Boolean,
    // Not present in older share texts (or stored results)
    val title: String? = null,
) : ResultInfo()
