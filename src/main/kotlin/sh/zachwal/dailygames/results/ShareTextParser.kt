package sh.zachwal.dailygames.results

import sh.zachwal.dailygames.db.jdbi.puzzle.Game
import sh.zachwal.dailygames.results.resultinfo.BandleInfo
import sh.zachwal.dailygames.results.resultinfo.BracketCityInfo
import sh.zachwal.dailygames.results.resultinfo.FlagleInfo
import sh.zachwal.dailygames.results.resultinfo.FramedInfo
import sh.zachwal.dailygames.results.resultinfo.GeoGridInfo
import sh.zachwal.dailygames.results.resultinfo.GeocirclesInfo
import sh.zachwal.dailygames.results.resultinfo.ParsedResult
import sh.zachwal.dailygames.results.resultinfo.PinpointInfo
import sh.zachwal.dailygames.results.resultinfo.Top5Info
import sh.zachwal.dailygames.results.resultinfo.TradleInfo
import sh.zachwal.dailygames.results.resultinfo.TravleInfo
import sh.zachwal.dailygames.results.resultinfo.WorldleInfo
import java.time.LocalDate
import java.time.format.DateTimeFormatter
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
            framedRegex.matches(shareText) -> Game.FRAMED
            shareText.contains("geogridgame") -> Game.GEOGRID
            shareText.trim().startsWith("Bandle") -> Game.BANDLE
            shareText.trim().startsWith("[Bracket City]") -> Game.BRACKET_CITY
            else -> null
        }
    }

    val worldleRegex = Regex(
        """\s*#Worldle\s+#(?<puzzleNumber>\d+)\s+\((?<day>\d{2})\.(?<month>\d{2})\.(?<year>\d{4})\)\s+(?<score>\S)/6\s+\((?<percentage>\d+)%\)[\s\S]*"""
    )

    fun extractWorldleInfo(shareText: String): ParsedResult {
        val match = worldleRegex.find(shareText) ?: throw IllegalArgumentException("Share text is not a Worldle share")
        val (puzzleNumber, day, month, year, score, percentage) = match.destructured
        val worldleInfo = WorldleInfo(
            percentage = percentage.toInt(),
        )
        return ParsedResult(
            puzzleNumber = puzzleNumber.toInt(),
            game = Game.WORLDLE,
            date = LocalDate.of(year.toInt(), month.toInt(), day.toInt()),
            score = score.toIntOrNull() ?: 7, // X / 6 scored as 7 points
            shareTextNoLink = shareText.substringBefore("https://").trim(),
            resultInfo = worldleInfo
        )
    }

    val tradleRegex = Regex(
        """
            \s*#Tradle\s+#(?<puzzleNumber>\d+)\s+(?<score>\S)/6[\s\S]*
        """.trimIndent()
    )

    fun extractTradleInfo(shareText: String): ParsedResult {
        val match = tradleRegex.find(shareText) ?: throw IllegalArgumentException("Share text is not a Tradle share")
        val (puzzleNumber, score) = match.destructured
        val tradleInfo = TradleInfo
        return ParsedResult(
            puzzleNumber = puzzleNumber.toInt(),
            game = Game.TRADLE,
            date = null,
            score = score.toIntOrNull() ?: 7, // X / 6 scored as 7 points.
            shareTextNoLink = shareText.substringBefore("https://").trim(),
            resultInfo = tradleInfo
        )
    }

    val puzzleNumberRegex = Regex("""#(?<puzzleNumber>\d+)""")
    val scoreRegex = Regex("""\+(?<score>\d+)""")
    val numAwayRegex = Regex("""\((?<numAway>\d+) away\)""")
    val hintCountRegex = Regex("""\((?<hintCount>\d+) hints?\)""")
    val guessEmojiRegex = Regex("[\uD83D\uDFE7\uD83D\uDFE9✅\uD83D\uDFE7\uD83D\uDFE5\uD83D\uDFE9✅]")
    val incorrectGuessEmojiRegex = Regex("[\uD83D\uDFE7\uD83D\uDFE5]")
    val checkboxEmojiRegex = Regex("✅")
    fun extractTravleInfo(shareText: String): ParsedResult {
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

        val travleInfo = TravleInfo(
            numGuesses = numGuesses,
            numIncorrect = numIncorrect,
            numPerfect = numPerfect,
            numHints = hintCount,
        )
        return ParsedResult(
            puzzleNumber = puzzleNumber.toInt(),
            game = Game.TRAVLE,
            date = null,
            score = score,
            shareTextNoLink = shareText.substringBefore("https://").trim(),
            resultInfo = travleInfo
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
    fun extractTop5Info(shareText: String): ParsedResult {
        val match = top5Regex.find(shareText) ?: throw IllegalArgumentException("Share text is not a Top 5 share")

        val (puzzleNumber) = match.destructured

        val isPerfect = perfectTop5Regex.find(shareText) != null
        val numGuesses = top5GuessRegex.findAll(shareText).count()
        val numCorrect = top5CorrectRegex.findAll(shareText).count()
        val livesAtStart = 5
        val score = livesAtStart - (numGuesses - numCorrect) + numCorrect
        val top5Info = Top5Info(
            numGuesses = numGuesses,
            numCorrect = numCorrect,
            isPerfect = isPerfect
        )
        return ParsedResult(
            puzzleNumber = puzzleNumber.toInt(),
            game = Game.TOP5,
            date = null,
            score = score,
            shareTextNoLink = shareText.substringBefore("https://").trim(),
            resultInfo = top5Info
        )
    }

    val flagleRegex = Regex(
        """
            \s*#Flagle\s+#(?<puzzleNumber>\d+)\s+\((?<day>\d{2})\.(?<month>\d{2})\.(?<year>\d{4})\)\s+(?<score>\S)/6\s+[\s\S]*
        """.trimIndent()
    )

    fun extractFlagleInfo(shareText: String): ParsedResult {
        val match = flagleRegex.find(shareText) ?: throw IllegalArgumentException("Share text is not a Flagle share")
        val (puzzleNumber, day, month, year, score) = match.destructured
        return ParsedResult(
            puzzleNumber = puzzleNumber.toInt(),
            game = Game.FLAGLE,
            date = LocalDate.of(year.toInt(), month.toInt(), day.toInt()),
            score = score.toIntOrNull() ?: 7, // X / 6 scored as 7 points
            shareTextNoLink = shareText.substringBefore("https://").trim(),
            resultInfo = FlagleInfo,
        )
    }

    val pinpointRegex = Regex(
        """
            \s*Pinpoint #(?<puzzleNumber>\d+)[\s\S]*\((?<score>\S)/5\)[\s\S]*
        """.trimIndent()
    )

    fun extractPinpointInfo(shareText: String): ParsedResult {
        val match =
            pinpointRegex.find(shareText) ?: throw IllegalArgumentException("Share text is not a Pinpoint share")
        val (puzzleNumber, score) = match.destructured
        return ParsedResult(
            puzzleNumber = puzzleNumber.toInt(),
            game = Game.PINPOINT,
            date = null,
            score = score.toIntOrNull() ?: 6, // X / 5 scored as 6 points
            shareTextNoLink = shareText.substringBefore("lnkd").trim(),
            resultInfo = PinpointInfo,
        )
    }

    val geocirclesRegex = Regex(
        """
            \s*Geocircles #(?<puzzleNumber>\d+)[\s\S]*
        """.trimIndent()
    )
    val greenCircleOrHeartRegex = Regex("(\uD83D\uDFE2|❤\uFE0F)")
    fun extractGeocirclesInfo(shareText: String): ParsedResult {
        val match =
            geocirclesRegex.find(shareText) ?: throw IllegalArgumentException("Share text is not a Geocircles share")
        val (puzzleNumber) = match.destructured
        val score = greenCircleOrHeartRegex.findAll(shareText).count()
        return ParsedResult(
            puzzleNumber = puzzleNumber.toInt(),
            game = Game.GEOCIRCLES,
            date = null,
            score = score,
            shareTextNoLink = shareText.substringBefore("https://").trim(),
            resultInfo = GeocirclesInfo
        )
    }

    val framedRegex = Regex(
        """
            \s*Framed #(?<puzzleNumber>\d+)[\s\S]*
        """.trimIndent()
    )
    val redSquareRegex = Regex("\uD83D\uDFE5")
    fun extractFramedInfo(shareText: String): ParsedResult {
        val match = framedRegex.find(shareText) ?: throw IllegalArgumentException("Share text is not a Framed share")
        val (puzzleNumber) = match.destructured
        val incorrectGuesses = redSquareRegex.findAll(shareText).count()
        val score = incorrectGuesses + 1
        return ParsedResult(
            puzzleNumber = puzzleNumber.toInt(),
            game = Game.FRAMED,
            date = null,
            score = score,
            shareTextNoLink = shareText.substringBefore("https://").trim(),
            resultInfo = FramedInfo
        )
    }

    fun extractGeoGridInfo(shareText: String): ParsedResult {
        val puzzleNumber = shareText
            .substringAfter("Board #")
            .substringBefore("\n")
            .trim()
            .toInt()
        val score = shareText
            .substringAfter("Score: ")
            .substringBefore("\n")
            .trim()
            .toDouble()
        val rank = shareText
            .substringAfter("Rank: ")
            .substringBefore(" /")
            .replace(",", "")
            .trim()
            .toInt()
        val rankOutOf = shareText
            .substringAfter(" / ")
            .substringBefore("\n")
            .replace(",", "")
            .trim()
            .toInt()
        val shareTextNoLink = shareText
            .substringBefore("https://")
            .lines()
            .filter { it.isNotBlank() }
            .filter { "Board" !in it }
            .filter { "Game Summary" !in it }
            .joinToString("\n")

        val numCorrect = shareText.count { it == '✅' }

        return ParsedResult(
            puzzleNumber = puzzleNumber,
            game = Game.GEOGRID,
            date = null,
            score = numCorrect,
            shareTextNoLink = shareTextNoLink,
            resultInfo = GeoGridInfo(
                score = score,
                rank = rank,
                rankOutOf = rankOutOf,
                numCorrect = numCorrect
            )
        )
    }

    private val skipRegex = Regex("⬛")
    private val correctBandRegex = Regex("\uD83D\uDFE8")
    private val incorrectRegex = Regex("\uD83D\uDFE5")
    fun extractBandleInfo(shareText: String): ParsedResult {
        val puzzleNumber = shareText
            .substringAfter("Bandle #")
            .substringBefore(" ")
            .trim()
            .toInt()

        val scoreText = shareText
            .trim()
            .lines()
            .first()
            .substringBefore("/")
            .substringAfterLast(" ")
        val score = if (scoreText == "x") 7 else scoreText.toInt()

        return ParsedResult(
            puzzleNumber = puzzleNumber,
            game = Game.BANDLE,
            date = null,
            score = score,
            shareTextNoLink = shareText.substringBefore("Found").trim(),
            resultInfo = BandleInfo(
                numSkips = skipRegex.findAll(shareText).count(),
                numCorrectBand = correctBandRegex.findAll(shareText).count(),
                numIncorrect = incorrectRegex.findAll(shareText).count(),
            )
        )
    }

    private val bracketCityDateTimeFormatter = DateTimeFormatter.ofPattern("MMMM d, yyyy")
    fun extractBracketCityInfo(shareText: String): ParsedResult {
        if (!shareText.contains("[Bracket City]")) {
            throw IllegalArgumentException("Share text is not a Bracket City share")
        }

        val rankRegex = Regex("""Rank: (\S+) \((.+?)\)""")
        val wrongGuessesRegex = Regex("""❌ Wrong guesses: (\d+)""")
        val peeksRegex = Regex("""👀 Peeks: (\d+)""")
        val answersRevealedRegex = Regex("""🛟 Answers Revealed: (\d+)""")
        val totalScoreRegex = Regex("""Total Score: ([\d.]+)""")
        val gridRegex = Regex("""\n([⬜🟩🟨🟥🟧🟦🟪]+)(?:\n|$)""")

        val rankMatch = rankRegex.find(shareText)
        val wrongGuessesMatch = wrongGuessesRegex.find(shareText)
        val peeksMatch = peeksRegex.find(shareText)
        val answersRevealedMatch = answersRevealedRegex.find(shareText)
        val totalScoreMatch = totalScoreRegex.find(shareText)
        val gridMatch = gridRegex.find(shareText)

        // Date is the second line and is formatted like April 20, 2025
        val date = LocalDate.parse(shareText.trim().lines()[1].trim(), bracketCityDateTimeFormatter)
        val puzzleNumber = date.year * 10000 + date.monthValue * 100 + date.dayOfMonth
        val rankEmoji = rankMatch?.groupValues?.get(1) ?: throw IllegalArgumentException("Rank emoji not found")
        val rank = rankMatch.groupValues[2]
        val wrongGuesses = wrongGuessesMatch?.groupValues?.get(1)?.toInt() ?: 0
        val peeks = peeksMatch?.groupValues?.get(1)?.toInt() ?: 0
        val answersRevealed = answersRevealedMatch?.groupValues?.get(1)?.toInt() ?: 0
        val totalScore = totalScoreMatch?.groupValues?.get(1)?.toDouble() ?: 0.0
        val grid = gridMatch?.groupValues?.get(1) ?: throw IllegalArgumentException("Grid not found")

        val bracketCityInfo = BracketCityInfo(
            rank = rank,
            rankEmoji = rankEmoji,
            wrongGuesses = wrongGuesses,
            peeks = peeks,
            answersRevealed = answersRevealed,
            totalScore = totalScore,
            grid = grid
        )

        return ParsedResult(
            puzzleNumber = puzzleNumber, // Bracket City does not include a puzzle number, calculate as YYYYMMDD
            game = Game.BRACKET_CITY,
            date = date,
            score = totalScore.toInt(), // Use total score as the score
            shareTextNoLink = shareText.substringAfter("https://www.theatlantic.com/games/bracket-city/").trim(),
            resultInfo = bracketCityInfo,
        )
    }
}
