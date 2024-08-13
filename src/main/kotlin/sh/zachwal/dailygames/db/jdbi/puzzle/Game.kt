package sh.zachwal.dailygames.db.jdbi.puzzle

import sh.zachwal.dailygames.utils.toSentenceCase

enum class Game {
    WORLDLE,
    TRADLE,
    TRAVLE,
    TOP5;

    fun displayName(): String {
        return when (this) {
            TOP5 -> "Top 5"
            else -> this.name.toSentenceCase()
        }
    }
}
