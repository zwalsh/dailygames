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
    BRACKET_CITY,
    SIZE_IT_UP,
    CARDLE,
    KRILLION,
    ;

    fun displayName(): String {
        return when (this) {
            TOP5 -> "Top 5"
            GEOGRID -> "GeoGrid"
            BRACKET_CITY -> "Bracket City"
            SIZE_IT_UP -> "Size It Up"
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
            BRACKET_CITY -> "\uD83C\uDFD9\uFE0F"
            SIZE_IT_UP -> "\uD83D\uDCD0"
            CARDLE -> "\uD83D\uDE97"
            KRILLION -> "\uD83E\uDD90"
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
            BRACKET_CITY -> "\uD83D\uDC51"
            SIZE_IT_UP -> "\uD83D\uDCAF"
            CARDLE -> "\uD83C\uDFC6"
            KRILLION -> "\uD83C\uDF1F"
        }
    }

    fun href(): String {
        return when (this) {
            WORLDLE -> "https://worldle.teuteuf.fr/"
            TRADLE -> "https://oec.world/en/games/tradle"
            TRAVLE -> "https://travle.earth/"
            TOP5 -> "https://topfivetrivia.com/"
            FLAGLE -> "https://www.flagle.io/"
            PINPOINT -> "https://www.linkedin.com/games/pinpoint/"
            GEOCIRCLES -> "https://geocircles.io/"
            FRAMED -> "https://framed.wtf/"
            GEOGRID -> "https://www.geogridgame.com/"
            BANDLE -> "https://bandle.app/"
            BRACKET_CITY -> "https://www.theatlantic.com/games/bracket-city/"
            SIZE_IT_UP -> "https://magnitudle.com/size-it-up"
            CARDLE -> "https://www.playcardle.com/"
            KRILLION -> "https://krillion.io"
        }
    }
}
