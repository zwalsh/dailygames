package sh.zachwal.dailygames.results.gamemapper

import sh.zachwal.dailygames.db.jdbi.User
import sh.zachwal.dailygames.db.jdbi.puzzle.Game
import sh.zachwal.dailygames.db.jdbi.puzzle.PuzzleResult
import sh.zachwal.dailygames.results.resultinfo.GeoGridInfo
import sh.zachwal.dailygames.results.resultinfo.ParsedResult
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GeoGridMapper @Inject constructor() : GameMapper {
    override val game = Game.GEOGRID

    override fun matches(shareText: String) = shareText.contains("geogridgame")

    override fun extract(shareText: String, user: User): ParsedResult {
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
                numCorrect = numCorrect,
            ),
        )
    }

    override fun shareLine(result: PuzzleResult): String = with(result) {
        val formattedScore = String.format("%.1f", info<GeoGridInfo>().score)
        val start = "${game.emoji()} ${game.displayName()} #$puzzleNumber $score/9 ($formattedScore)"
        return if (score == 9) {
            "$start ${game.perfectEmoji()}"
        } else {
            start
        }
    }
}
