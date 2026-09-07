package sh.zachwal.dailygames.results.gamemapper

import sh.zachwal.dailygames.db.jdbi.User
import sh.zachwal.dailygames.db.jdbi.puzzle.Game
import sh.zachwal.dailygames.db.jdbi.puzzle.PuzzleResult
import sh.zachwal.dailygames.results.resultinfo.ParsedResult
import sh.zachwal.dailygames.results.resultinfo.SizeItUpInfo
import sh.zachwal.dailygames.users.UserPreferencesService
import java.time.Clock
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SizeItUpMapper @Inject constructor(
    private val clock: Clock,
    private val userPreferencesService: UserPreferencesService,
) : GameMapper {
    override val game = Game.SIZE_IT_UP

    private val sizeItUpRowRegex = Regex("^(?:🟥|⬜)+$")
    private val sizeItUpFilledSquareRegex = Regex("🟥")

    override fun matches(shareText: String) = shareText.trim().startsWith("Size It Up")

    override fun extract(shareText: String, user: User): ParsedResult {
        if (!shareText.trim().startsWith("Size It Up")) {
            throw IllegalArgumentException("Share text is not a Size It Up share")
        }

        val date = LocalDate.now(clock.withZone(userPreferencesService.getTimeZone(user.id)))

        val score = shareText
            .substringAfter("Overall Score ")
            .substringBefore("\n")
            .trim()
            .toInt()
        val puzzleNumber = date.year * 10000 + date.monthValue * 100 + date.dayOfMonth
        val roundScores = shareText
            .lines()
            .map { it.trim() }
            .filter { sizeItUpRowRegex.matches(it) }
            .map { row -> sizeItUpFilledSquareRegex.findAll(row).count() }

        return ParsedResult(
            puzzleNumber = puzzleNumber, // Size It Up does not include a puzzle number or date, calculate as YYYYMMDD
            game = Game.SIZE_IT_UP,
            date = date,
            score = score,
            shareTextNoLink = shareText.substringBefore("https://").trim(),
            resultInfo = SizeItUpInfo(roundScores = roundScores),
        )
    }

    override fun shareLine(result: PuzzleResult): String = with(result) {
        val gameDate = puzzleDate?.let { " ${it.monthValue}/${"%02d".format(it.dayOfMonth)}" } ?: ""
        val start = "${game.emoji()} ${game.displayName()}$gameDate $score/500"
        return if (score == 500) {
            "$start ${game.perfectEmoji()}"
        } else {
            start
        }
    }
}
