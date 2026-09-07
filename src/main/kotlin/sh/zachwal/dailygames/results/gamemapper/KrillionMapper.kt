package sh.zachwal.dailygames.results.gamemapper

import sh.zachwal.dailygames.db.jdbi.User
import sh.zachwal.dailygames.db.jdbi.puzzle.Game
import sh.zachwal.dailygames.db.jdbi.puzzle.PuzzleResult
import sh.zachwal.dailygames.results.resultinfo.KrillionInfo
import sh.zachwal.dailygames.results.resultinfo.ParsedResult
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class KrillionMapper @Inject constructor() : GameMapper {
    override val game = Game.KRILLION

    private val missRegex = Regex("⬛")
    private val planktonRegex = Regex("🫧")
    private val schoolerRegex = Regex("🐟")
    private val rareRegex = Regex("🦑")
    private val deepCutRegex = Regex("🏮")
    private val krillionRegex = Regex("🌟")

    override fun matches(shareText: String) = shareText.trim().startsWith("Krillion #")

    override fun extract(shareText: String, user: User): ParsedResult {
        val puzzleNumber = shareText
            .substringAfter("Krillion #")
            .substringBefore("\n")
            .trim()
            .takeWhile { it.isDigit() }
            .toInt()

        val nonBlankLines = shareText.lines().map { it.trim() }.filter { it.isNotBlank() }
        val rawScore = nonBlankLines[1].toInt()

        return ParsedResult(
            puzzleNumber = puzzleNumber,
            game = Game.KRILLION,
            date = null,
            // Krillion's raw score is out of 700 (7 rounds, up to 100 each). Normalize to a 0-10 scale for points.
            score = rawScore / 70,
            shareTextNoLink = shareText.substringBefore("https://").trim(),
            resultInfo = KrillionInfo(
                rawScore = rawScore,
                missCount = missRegex.findAll(shareText).count(),
                planktonCount = planktonRegex.findAll(shareText).count(),
                schoolerCount = schoolerRegex.findAll(shareText).count(),
                rareCount = rareRegex.findAll(shareText).count(),
                deepCutCount = deepCutRegex.findAll(shareText).count(),
                krillionCount = krillionRegex.findAll(shareText).count(),
            ),
        )
    }

    override fun shareLine(result: PuzzleResult): String = with(result) {
        val krillionInfo = info<KrillionInfo>()
        val rawScore = krillionInfo.rawScore
        val start = "${game.emoji()} ${game.displayName()} #$puzzleNumber $rawScore/700"
        if (krillionInfo.krillionCount >= 1) "$start ${game.perfectEmoji()}" else start
    }
}
