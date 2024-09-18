package sh.zachwal.dailygames.db.jdbi.puzzle

import sh.zachwal.dailygames.utils.toSentenceCase

enum class Game {
    WORLDLE,
    TRADLE,
    TRAVLE,
    TOP5,
    FLAGLE,
    PINPOINT,
    GEOCIRCLES;

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
            PINPOINT -> "\uD83D\uDCCC"
            GEOCIRCLES -> TODO()
        }
    }

    fun href(): String {
        return when (this) {
            WORLDLE -> "https://worldle.teuteuf.fr/"
            TRADLE -> "https://games.oec.world/en/tradle/"
            TRAVLE -> "https://travle.earth/"
            TOP5 -> "https://top5-game.com/"
            FLAGLE -> "https://www.flagle.io/"
            PINPOINT -> "https://www.linkedin.com/games/pinpoint/"
            GEOCIRCLES -> TODO()
        }
    }
}
