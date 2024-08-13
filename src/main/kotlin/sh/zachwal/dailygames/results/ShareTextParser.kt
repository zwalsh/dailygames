package sh.zachwal.dailygames.results

import sh.zachwal.dailygames.db.jdbi.puzzle.Game
import sh.zachwal.dailygames.results.gameinfo.TradleInfo
import sh.zachwal.dailygames.results.gameinfo.TravleInfo
import sh.zachwal.dailygames.results.gameinfo.WorldleInfo
import java.time.LocalDate
import javax.inject.Singleton

@Singleton
class ShareTextParser {

    val worldleRegex = Regex(
        """\s*#Worldle\s+#(?<puzzleNumber>\d+)\s+\((?<day>\d{2})\.(?<month>\d{2})\.(?<year>\d{4})\)\s+(?<score>\S)/6\s+\((?<percentage>\d+)%\)[\s\S]*"""
    )

    val tradleRegex = Regex(
        """
            \s*#Tradle\s+#(?<puzzleNumber>\d+)\s+(?<score>\S)/6[\s\S]*
        """.trimIndent()
    )

    val travleRegex = Regex(
        """
            \s*#travle\s+#(?<puzzleNumber>\d+)\s+\+(?<score>\d+)\s*(\((?<hintCount>\d+) hint.*\))?[\s\S]*
        """.trimIndent()
    )

    val guessEmojiRegex = Regex("[\uD83D\uDFE7\uD83D\uDFE9✅]")

    val orangeSquareEmojiRegex = Regex("\uD83D\uDFE7")

    val checkboxEmojiRegex = Regex("✅")

    val top5Regex = Regex(
        """
            \s*Top 5\s+#(?<puzzleNumber>\d+)[\s\S]*
        """.trimIndent()
    )

    fun identifyGame(shareText: String): Game? {
        return when {
            worldleRegex.matches(shareText) -> Game.WORLDLE
            tradleRegex.matches(shareText) -> Game.TRADLE
            travleRegex.matches(shareText) -> Game.TRAVLE
            top5Regex.matches(shareText) -> Game.TOP5
            else -> null
        }
    }

    fun extractWorldleInfo(shareText: String): WorldleInfo {
        val match = worldleRegex.find(shareText) ?: throw IllegalArgumentException("Share text is not a Worldle share")
        val (puzzleNumber, day, month, year, score, percentage) = match.destructured
        return WorldleInfo(
            puzzleNumber = puzzleNumber.toInt(),
            date = LocalDate.of(year.toInt(), month.toInt(), day.toInt()),
            score = score.toIntOrNull() ?: 0,
            percentage = percentage.toInt(),
            shareTextNoLink = shareText.substringBefore("https://").trim()
        )
    }

    fun extractTradleInfo(shareText: String): TradleInfo {
        val match = tradleRegex.find(shareText) ?: throw IllegalArgumentException("Share text is not a Tradle share")
        val (puzzleNumber, score) = match.destructured
        return TradleInfo(
            puzzleNumber = puzzleNumber.toInt(),
            score = score.toIntOrNull() ?: 0,
            shareTextNoLink = shareText.substringBefore("https://").trim()
        )
    }

    fun extractTravleInfo(shareText: String): TravleInfo {
        val match = travleRegex.find(shareText) ?: throw IllegalArgumentException("Share text is not a Travle share")

        val (puzzleNumber, score, _, hintCount) = match.destructured

        val numGuesses = guessEmojiRegex.findAll(shareText).count()
        val numIncorrect = orangeSquareEmojiRegex.findAll(shareText).count()
        val numPerfect = checkboxEmojiRegex.findAll(shareText).count()

        return TravleInfo(
            puzzleNumber = puzzleNumber.toInt(),
            score = score.toInt(),
            shareTextNoLink = shareText.substringBefore("https://").trim(),
            numGuesses = numGuesses,
            numIncorrect = numIncorrect,
            numPerfect = numPerfect,
            numHints = hintCount.toIntOrNull() ?: 0
        )
    }
}
