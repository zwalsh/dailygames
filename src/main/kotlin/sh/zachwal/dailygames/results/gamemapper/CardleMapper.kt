package sh.zachwal.dailygames.results.gamemapper

import sh.zachwal.dailygames.db.jdbi.User
import sh.zachwal.dailygames.db.jdbi.puzzle.Game
import sh.zachwal.dailygames.db.jdbi.puzzle.PuzzleResult
import sh.zachwal.dailygames.results.resultinfo.CardleInfo
import sh.zachwal.dailygames.results.resultinfo.ParsedResult
import sh.zachwal.dailygames.users.UserPreferencesService
import java.time.Clock
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CardleMapper @Inject constructor(
    private val clock: Clock,
    private val userPreferencesService: UserPreferencesService,
) : GameMapper {
    override val game = Game.CARDLE

    private val cardleHeaderRegex = Regex("""Cardle\s+(?<guesses>\d)/5""")
    private val cardleStreakRegex = Regex("""Streak\s+(?<streak>\d+)""")
    private val cardleScoreRegex = Regex("""Total Score\s+(?<score>\d+)""")

    override fun matches(shareText: String) = shareText.trim().startsWith("Cardle")

    override fun extract(shareText: String, user: User): ParsedResult {
        if (!shareText.trim().startsWith("Cardle")) {
            throw IllegalArgumentException("Share text is not a Cardle share")
        }

        val date = LocalDate.now(clock.withZone(userPreferencesService.getTimeZone(user.id)))

        val numGuesses = cardleHeaderRegex.find(shareText)?.groups?.get("guesses")?.value?.toInt()
            ?: throw IllegalArgumentException("Number of guesses not found")
        // Streak and Total Score lines are absent when the streak/score is 0
        val streak = cardleStreakRegex.find(shareText)?.groups?.get("streak")?.value?.toInt() ?: 0
        val score = cardleScoreRegex.find(shareText)?.groups?.get("score")?.value?.toInt() ?: 0
        val puzzleNumber = date.year * 10000 + date.monthValue * 100 + date.dayOfMonth

        return ParsedResult(
            puzzleNumber = puzzleNumber, // Cardle does not include a puzzle number or date, calculate as YYYYMMDD
            game = Game.CARDLE,
            date = date,
            score = score,
            shareTextNoLink = shareText.substringBefore("https://").trim(),
            resultInfo = CardleInfo(numGuesses = numGuesses, streak = streak),
        )
    }

    override fun shareLine(result: PuzzleResult): String = with(result) {
        val gameDate = puzzleDate?.let { " ${it.monthValue}/${"%02d".format(it.dayOfMonth)}" } ?: ""
        val start = "${game.emoji()} ${game.displayName()}$gameDate $score/15"
        return if (score == 15) {
            "$start ${game.perfectEmoji()}"
        } else {
            start
        }
    }
}
