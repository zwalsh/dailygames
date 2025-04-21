package sh.zachwal.dailygames.db.jdbi.puzzle

import sh.zachwal.dailygames.utils.toSentenceCase

enum class Game {
    WORLDLE,
    TRADLE,
    TRAVLE,
    TOP5,
    FLAGLE,
    PINPOINT,
    GEOCIRCLES,
    FRAMED,
    GEOGRID,
    BANDLE,
    BRACKET_CITY;

    fun displayName(): String {
        return when (this) {
            TOP5 -> "Top 5"
            GEOGRID -> "GeoGrid"
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
            GEOCIRCLES -> "\uD83D\uDFE2"
            FRAMED -> "\uD83C\uDFA5"
            GEOGRID -> "\uD83C\uDF10"
            BANDLE -> "\uD83C\uDFB8"
            BRACKET_CITY -> TODO()
        }
    }

    fun perfectEmoji(): String {
        return when (this) {
            WORLDLE -> "\uD83D\uDCCD"
            TRADLE -> "\uD83D\uDCE6"
            TRAVLE -> "✅"
            TOP5 -> "\uD83C\uDF08"
            FLAGLE -> "\uD83C\uDFC1"
            PINPOINT -> this.emoji()
            GEOCIRCLES -> "\uD83C\uDFAF"
            FRAMED -> "\uD83C\uDF7F"
            GEOGRID -> "✅"
            BANDLE -> "\uD83C\uDFB5"
            BRACKET_CITY -> TODO()
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
            GEOCIRCLES -> "https://geocircles.io/"
            FRAMED -> "https://framed.wtf/"
            GEOGRID -> "https://www.geogridgame.com/"
            BANDLE -> "https://bandle.app/"
            BRACKET_CITY -> "https://www.theatlantic.com/games/bracket-city/"
        }
    }
}
