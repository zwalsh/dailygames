package sh.zachwal.dailygames.results

import sh.zachwal.dailygames.db.jdbi.puzzle.Game
import sh.zachwal.dailygames.results.gameinfo.FlagleInfo
import sh.zachwal.dailygames.results.gameinfo.GeocirclesInfo
import sh.zachwal.dailygames.results.gameinfo.PinpointInfo
import sh.zachwal.dailygames.results.gameinfo.Top5Info
import sh.zachwal.dailygames.results.gameinfo.TradleInfo
import sh.zachwal.dailygames.results.gameinfo.TravleInfo
import sh.zachwal.dailygames.results.gameinfo.WorldleInfo
import java.time.LocalDate
import javax.inject.Singleton

@Singleton
class ShareTextParser {

    fun identifyGame(shareText: String): Game? {
        return when {
            worldleRegex.matches(shareText) -> Game.WORLDLE
            tradleRegex.matches(shareText) -> Game.TRADLE
            shareText.contains("#travle") -> Game.TRAVLE
            top5Regex.matches(shareText) -> Game.TOP5
            flagleRegex.matches(shareText) -> Game.FLAGLE
            pinpointRegex.matches(shareText) -> Game.PINPOINT
            geocirclesRegex.matches(shareText) -> Game.GEOCIRCLES
            else -> null
        }
    }

    val worldleRegex = Regex(
        """\s*#Worldle\s+#(?<puzzleNumber>\d+)\s+\((?<day>\d{2})\.(?<month>\d{2})\.(?<year>\d{4})\)\s+(?<score>\S)/6\s+\((?<percentage>\d+)%\)[\s\S]*"""
    )
    fun extractWorldleInfo(shareText: String): WorldleInfo {
        val match = worldleRegex.find(shareText) ?: throw IllegalArgumentException("Share text is not a Worldle share")
        val (puzzleNumber, day, month, year, score, percentage) = match.destructured
        return WorldleInfo(
            puzzleNumber = puzzleNumber.toInt(),
            date = LocalDate.of(year.toInt(), month.toInt(), day.toInt()),
            score = score.toIntOrNull() ?: 7, // X / 6 scored as 7 points
            percentage = percentage.toInt(),
            shareTextNoLink = shareText.substringBefore("https://").trim()
        )
    }

    val tradleRegex = Regex(
        """
            \s*#Tradle\s+#(?<puzzleNumber>\d+)\s+(?<score>\S)/6[\s\S]*
        """.trimIndent()
    )
    fun extractTradleInfo(shareText: String): TradleInfo {
        val match = tradleRegex.find(shareText) ?: throw IllegalArgumentException("Share text is not a Tradle share")
        val (puzzleNumber, score) = match.destructured
        return TradleInfo(
            puzzleNumber = puzzleNumber.toInt(),
            score = score.toIntOrNull() ?: 7, // X / 6 scored as 7 points.
            shareTextNoLink = shareText.substringBefore("https://").trim()
        )
    }

    val puzzleNumberRegex = Regex("""#(?<puzzleNumber>\d+)""")
    val scoreRegex = Regex("""\+(?<score>\d+)""")
    val numAwayRegex = Regex("""\((?<numAway>\d+) away\)""")
    val hintCountRegex = Regex("""\((?<hintCount>\d+) hints?\)""")
    val guessEmojiRegex = Regex("[\uD83D\uDFE7\uD83D\uDFE9✅\uD83D\uDFE7\uD83D\uDFE5\uD83D\uDFE9✅]")
    val incorrectGuessEmojiRegex = Regex("[\uD83D\uDFE7\uD83D\uDFE5]")
    val checkboxEmojiRegex = Regex("✅")
    fun extractTravleInfo(shareText: String): TravleInfo {
        if (!shareText.contains("#travle")) {
            throw IllegalArgumentException("Share text is not a Travle share")
        }

        val puzzleNumber = puzzleNumberRegex.find(shareText)?.groupValues?.get(1)
            ?: throw IllegalArgumentException("Puzzle number not found")
        val score = scoreRegex.find(shareText)?.groupValues?.get(1)?.toInt()
            // Use the number away times negative one as the score if the result is a Did Not Finish
            ?: numAwayRegex.find(shareText)?.groupValues?.get(1)?.toInt()?.times(-1)
            ?: throw IllegalArgumentException("Score not found")
        val numGuesses = guessEmojiRegex.findAll(shareText).count()
        val numIncorrect = incorrectGuessEmojiRegex.findAll(shareText).count()
        val numPerfect = checkboxEmojiRegex.findAll(shareText).count()
        val hintCount = hintCountRegex.find(shareText)?.groupValues?.get(1)?.toIntOrNull() ?: 0

        return TravleInfo(
            puzzleNumber = puzzleNumber.toInt(),
            score = score,
            shareTextNoLink = shareText.substringBefore("https://").trim(),
            numGuesses = numGuesses,
            numIncorrect = numIncorrect,
            numPerfect = numPerfect,
            numHints = hintCount,
        )
    }

    val top5Regex = Regex(
        """
            \s*Top 5\s+#(?<puzzleNumber>\d+)[\s\S]*
        """.trimIndent()
    )
    val perfectTop5Regex = Regex("\uD83D\uDFE5\uD83D\uDFE7\uD83D\uDFE8\uD83D\uDFE9\uD83D\uDFE6")
    val top5GuessRegex = Regex("[\uD83D\uDFE5\uD83D\uDFE7\uD83D\uDFE8\uD83D\uDFE9\uD83D\uDFE6⬜]")
    val top5CorrectRegex = Regex("[[\uD83D\uDFE5\uD83D\uDFE7\uD83D\uDFE8\uD83D\uDFE9\uD83D\uDFE6]]")
    fun extractTop5Info(shareText: String): Top5Info {
        val match = top5Regex.find(shareText) ?: throw IllegalArgumentException("Share text is not a Top 5 share")

        val (puzzleNumber) = match.destructured

        val isPerfect = perfectTop5Regex.find(shareText) != null
        val numGuesses = top5GuessRegex.findAll(shareText).count()
        val numCorrect = top5CorrectRegex.findAll(shareText).count()
        val livesAtStart = 5
        val score = livesAtStart - (numGuesses - numCorrect) + numCorrect
        return Top5Info(
            puzzleNumber = puzzleNumber.toInt(),
            score = score,
            shareTextNoLink = shareText.substringBefore("https://").trim(),
            numGuesses = numGuesses,
            numCorrect = numCorrect,
            isPerfect = isPerfect
        )
    }

    val flagleRegex = Regex(
        """
            \s*#Flagle\s+#(?<puzzleNumber>\d+)\s+\((?<day>\d{2})\.(?<month>\d{2})\.(?<year>\d{4})\)\s+(?<score>\S)/6\s+[\s\S]*
        """.trimIndent()
    )
    fun extractFlagleInfo(shareText: String): FlagleInfo {
        val match = flagleRegex.find(shareText) ?: throw IllegalArgumentException("Share text is not a Flagle share")
        val (puzzleNumber, day, month, year, score) = match.destructured
        return FlagleInfo(
            puzzleNumber = puzzleNumber.toInt(),
            date = LocalDate.of(year.toInt(), month.toInt(), day.toInt()),
            score = score.toIntOrNull() ?: 7, // X / 6 scored as 7 points
            shareTextNoLink = shareText.substringBefore("https://").trim()
        )
    }

    val pinpointRegex = Regex(
        """
            \s*Pinpoint #(?<puzzleNumber>\d+)[\s\S]*\((?<score>\S)/5\)\s+[\s\S]*
        """.trimIndent()
    )
    fun extractPinpointInfo(shareText: String): PinpointInfo {
        val match = pinpointRegex.find(shareText) ?: throw IllegalArgumentException("Share text is not a Pinpoint share")
        val (puzzleNumber, score) = match.destructured
        return PinpointInfo(
            puzzleNumber = puzzleNumber.toInt(),
            score = score.toIntOrNull() ?: 6, // X / 5 scored as 6 points
            shareTextNoLink = shareText.substringBefore("lnkd").trim(),
        )
    }

    val geocirclesRegex = Regex(
        """
            \s*Geocircles #(?<puzzleNumber>\d+)[\s\S]*
        """.trimIndent()
    )
    val greenCircleOrHeartRegex = Regex("(\uD83D\uDFE2|❤\uFE0F)")
    fun extractGeocirclesInfo(shareText: String): GeocirclesInfo {
        val match = geocirclesRegex.find(shareText) ?: throw IllegalArgumentException("Share text is not a Geocircles share")
        val (puzzleNumber) = match.destructured
        val score = greenCircleOrHeartRegex.findAll(shareText).count()
        return GeocirclesInfo(
            puzzleNumber = puzzleNumber.toInt(),
            score = score,
            shareTextNoLink = shareText.substringBefore("https://").trim()
        )
    }
}
