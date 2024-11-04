package sh.zachwal.dailygames.admin

import org.jdbi.v3.core.Jdbi
import org.slf4j.LoggerFactory
import sh.zachwal.dailygames.admin.views.BackfillPageView
import sh.zachwal.dailygames.db.dao.game.PuzzleDAO
import sh.zachwal.dailygames.db.dao.game.ResultDAO
import sh.zachwal.dailygames.db.jdbi.puzzle.FlagleResult
import sh.zachwal.dailygames.db.jdbi.puzzle.Game
import sh.zachwal.dailygames.db.jdbi.puzzle.GeocirclesResult
import sh.zachwal.dailygames.db.jdbi.puzzle.PinpointResult
import sh.zachwal.dailygames.db.jdbi.puzzle.Puzzle
import sh.zachwal.dailygames.db.jdbi.puzzle.Top5Result
import sh.zachwal.dailygames.db.jdbi.puzzle.TradleResult
import sh.zachwal.dailygames.db.jdbi.puzzle.TravleResult
import sh.zachwal.dailygames.db.jdbi.puzzle.WorldleResult
import sh.zachwal.dailygames.nav.NavItem
import sh.zachwal.dailygames.nav.NavViewFactory
import sh.zachwal.dailygames.results.ResultService
import sh.zachwal.dailygames.results.resultinfo.FlagleInfo
import sh.zachwal.dailygames.results.resultinfo.GeocirclesInfo
import sh.zachwal.dailygames.results.resultinfo.PinpointInfo
import sh.zachwal.dailygames.results.resultinfo.Top5Info
import sh.zachwal.dailygames.results.resultinfo.TradleInfo
import sh.zachwal.dailygames.results.resultinfo.TravleInfo
import sh.zachwal.dailygames.results.resultinfo.WorldleInfo
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.streams.toList

@Singleton
class BackfillService @Inject constructor(
    private val navViewFactory: NavViewFactory,
    private val resultService: ResultService,
    private val resultDAO: ResultDAO,
    private val jdbi: Jdbi,
) {

    private val logger = LoggerFactory.getLogger(BackfillService::class.java)

    fun backfillView(): BackfillPageView {
        return BackfillPageView(
            navView = navViewFactory.navView("zach", NavItem.PROFILE),
            success = false,
            failure = false,
            resultsBackfilled = null,
            resultsExisting = null,
            resultsFailed = null,
        )
    }

    fun backfillAllResults(): BackfillPageView {
        val result = runBackfill()
        return BackfillPageView(
            navView = navViewFactory.navView("zach", NavItem.PROFILE),
            success = result.failed == 0,
            failure = result.failed > 0,
            resultsBackfilled = result.backfilled,
            resultsExisting = result.existing,
            resultsFailed = result.failed,
        )
    }

    data class BackfillResult(
        val backfilled: Int,
        val existing: Int,
        val failed: Int,
    ) {
        operator fun plus(other: BackfillResult): BackfillResult {
            return BackfillResult(
                backfilled = backfilled + other.backfilled,
                existing = existing + other.existing,
                failed = failed + other.failed,
            )
        }
    }

    private fun runBackfill(): BackfillResult {
        logger.info("Starting backfill")

        var backfillResult = BackfillResult(0, 0, 0)

        Game.values().forEach { game ->
            val result = backfillGame(game)
            backfillResult += result
        }

        return backfillResult
    }

    private fun backfillGame(
        game: Game
    ): BackfillResult {
        var existing = 0
        var backfilled = 0
        var failed = 0
        logger.info("Backfilling game $game")
        val puzzles = jdbi.open().use { handle ->
            val puzzleDAO = handle.attach(PuzzleDAO::class.java)
            puzzleDAO.listPuzzlesForGameDescending(game).toList()
        }

        puzzles.forEach { puzzle ->
            logger.info("Backfilling puzzle $puzzle")
            val results = resultService.allResultsForPuzzle(puzzle).sortedBy { it.instantSubmitted }
            logger.info("Found ${results.size} results for puzzle $puzzle")
            results.forEach { result ->
                logger.info("Attempting to backfill result $result")
                val existingResult = resultDAO.findResult(result.userId, puzzle)
                if (existingResult != null) {
                    if (game != Game.FLAGLE) {
                        logger.info("Result already exists, skipping")
                        existing++
                        return@forEach
                    } else {
                        logger.warn("Flagle result already exists, attempting to backfill anyway due to Flagle issues with puzzle number.")
                    }
                }

                try {
                    when (result) {
                        is WorldleResult -> result.tryBackfill()
                        is TradleResult -> result.tryBackfill()
                        is TravleResult -> result.tryBackfill()
                        is Top5Result -> result.tryBackfill()
                        is FlagleResult -> result.tryBackfill()
                        is PinpointResult -> result.tryBackfill()
                        is GeocirclesResult -> result.tryBackfill()
                    }
                    backfilled++
                } catch (e: Exception) {
                    logger.error("Failed to backfill result $result", e)
                    failed++
                }
            }
        }
        return BackfillResult(backfilled, existing, failed)
    }

    private fun WorldleResult.tryBackfill() {
        resultDAO.insertResult(
            userId = userId,
            puzzle = Puzzle(game, puzzleNumber, puzzleDate),
            score = score,
            shareText = shareText,
            resultInfo = WorldleInfo(
                percentage = scorePercentage
            )
        )
    }

    private fun TradleResult.tryBackfill() {
        resultDAO.insertResult(
            userId = userId,
            puzzle = Puzzle(game, puzzleNumber, puzzleDate),
            score = score,
            shareText = shareText,
            resultInfo = TradleInfo,
        )
    }

    private fun TravleResult.tryBackfill() {
        resultDAO.insertResult(
            userId = userId,
            puzzle = Puzzle(game, puzzleNumber, puzzleDate),
            score = score,
            shareText = shareText,
            resultInfo = TravleInfo(
                numGuesses = numGuesses,
                numIncorrect = numIncorrect,
                numPerfect = numPerfect,
                numHints = numHints,
            ),
        )
    }

    private fun Top5Result.tryBackfill() {
        resultDAO.insertResult(
            userId = userId,
            puzzle = Puzzle(game, puzzleNumber, puzzleDate),
            score = score,
            shareText = shareText,
            resultInfo = Top5Info(
                numGuesses = numGuesses,
                numCorrect = numCorrect,
                isPerfect = isPerfect,
            ),
        )
    }

    private fun FlagleResult.tryBackfill() {
        resultDAO.insertResult(
            userId = userId,
            puzzle = Puzzle(game, puzzleNumber, puzzleDate),
            score = score,
            shareText = shareText,
            resultInfo = FlagleInfo,
        )
    }

    private fun PinpointResult.tryBackfill() {
        resultDAO.insertResult(
            userId = userId,
            puzzle = Puzzle(game, puzzleNumber, puzzleDate),
            score = score,
            shareText = shareText,
            resultInfo = PinpointInfo,
        )
    }

    private fun GeocirclesResult.tryBackfill() {
        resultDAO.insertResult(
            userId = userId,
            puzzle = Puzzle(game, puzzleNumber, puzzleDate),
            score = score,
            shareText = shareText,
            resultInfo = GeocirclesInfo,
        )
    }
}