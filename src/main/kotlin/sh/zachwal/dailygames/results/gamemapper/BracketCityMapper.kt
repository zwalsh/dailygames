package sh.zachwal.dailygames.results.gamemapper

import sh.zachwal.dailygames.db.jdbi.User
import sh.zachwal.dailygames.db.jdbi.puzzle.Game
import sh.zachwal.dailygames.db.jdbi.puzzle.PuzzleResult
import sh.zachwal.dailygames.results.resultinfo.BracketCityInfo
import sh.zachwal.dailygames.results.resultinfo.ParsedResult
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BracketCityMapper @Inject constructor() : GameMapper {
    override val game = Game.BRACKET_CITY

    private val bracketCityDateTimeFormatter = DateTimeFormatter.ofPattern("MMMM d, yyyy")

    override fun matches(shareText: String) = shareText.trim().startsWith("[Bracket City]")

    override fun extract(shareText: String, user: User): ParsedResult {
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
            grid = grid,
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

    override fun shareLine(result: PuzzleResult): String = with(result) {
        val info = info<BracketCityInfo>()
        val gameDate = puzzleDate?.let { " ${it.monthValue}/${it.dayOfMonth}" } ?: ""
        return "${game.emoji()} ${game.displayName()}$gameDate $score.0 ${info.rankEmoji}"
    }
}
