package sh.zachwal.dailygames.results.gamemapper

import sh.zachwal.dailygames.db.jdbi.User
import sh.zachwal.dailygames.db.jdbi.puzzle.Game
import sh.zachwal.dailygames.db.jdbi.puzzle.PuzzleResult
import sh.zachwal.dailygames.results.resultinfo.MapTapInfo
import sh.zachwal.dailygames.results.resultinfo.ParsedResult
import sh.zachwal.dailygames.users.UserPreferencesService
import java.time.Clock
import java.time.LocalDate
import java.time.MonthDay
import java.time.format.DateTimeFormatter
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MapTapMapper @Inject constructor(
    private val clock: Clock,
    private val userPreferencesService: UserPreferencesService,
) : GameMapper {
    override val game = Game.MAPTAP

    private val firstLineRegex = Regex("""^(?:www\.)?maptap\.gg .+$""")
    private val monthDayFormatter = DateTimeFormatter.ofPattern("MMMM d", Locale.ENGLISH)

    override fun matches(shareText: String): Boolean {
        val firstLine = shareText.trim().lines().firstOrNull() ?: return false
        return firstLineRegex.matches(firstLine)
    }

    override fun extract(shareText: String, user: User): ParsedResult {
        val lines = shareText.trim().lines().map { it.trim() }.filter { it.isNotBlank() }

        // MapTap does not include a puzzle number, only a date like "September 7" with no year.
        // Combine the parsed month/day with the user's current year to get a full date.
        val monthDay = MonthDay.parse(lines[0].substringAfter("maptap.gg").trim(), monthDayFormatter)
        val year = LocalDate.now(clock.withZone(userPreferencesService.getTimeZone(user.id))).year
        val date = monthDay.atYear(year)
        val puzzleNumber = date.year * 10000 + date.monthValue * 100 + date.dayOfMonth

        val roundScores = lines[1].split(" ").map { round -> round.takeWhile { it.isDigit() }.toInt() }
        val finalScore = lines[2].substringAfter("Final score:").trim().toInt()

        return ParsedResult(
            puzzleNumber = puzzleNumber,
            game = Game.MAPTAP,
            date = date,
            score = finalScore,
            shareTextNoLink = shareText.substringBefore("https://").trim(),
            resultInfo = MapTapInfo(finalScore = finalScore, roundScores = roundScores),
        )
    }

    override fun shareLine(result: PuzzleResult): String = with(result) {
        val mapTapInfo = info<MapTapInfo>()
        val gameDate = puzzleDate?.let { " ${it.monthValue}/${"%02d".format(it.dayOfMonth)}" } ?: ""
        val start = "${game.emoji()} ${game.displayName()}$gameDate ${mapTapInfo.finalScore}/1000"
        return if (mapTapInfo.roundScores.any { it == 100 }) "$start ${game.perfectEmoji()}" else start
    }
}
