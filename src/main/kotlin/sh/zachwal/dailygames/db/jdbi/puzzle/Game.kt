package sh.zachwal.dailygames.db.jdbi.puzzle

import sh.zachwal.dailygames.utils.toSentenceCase

enum class Game {
    WORLDLE,
    TRADLE,
    TRAVLE,
    TOP5,
    FLAGLE;

    fun displayName(): String {
        return when (this) {
            TOP5 -> "Top 5"
            else -> this.name.toSentenceCase()
        }
    }

    fun emoji(): String {
        return when (this) {
            WORLDLE -> "\uD83C\uDF0D"
            TRADLE -> "\uD83D\uDEA2"
            TRAVLE -> "\uD83E\uDDED"
            TOP5 -> "\uD83E\uDDE0"
            FLAGLE -> "\uD83D\uDEA9"
        }
    }
}
