package sh.zachwal.dailygames.results

import org.jdbi.v3.core.Handle
import org.jdbi.v3.core.Jdbi
import org.slf4j.LoggerFactory
import sh.zachwal.dailygames.chat.chatLink
import sh.zachwal.dailygames.db.dao.game.FlagleDAO
import sh.zachwal.dailygames.db.dao.game.GeocirclesDAO
import sh.zachwal.dailygames.db.dao.game.PinpointDAO
import sh.zachwal.dailygames.db.dao.game.PuzzleDAO
import sh.zachwal.dailygames.db.dao.game.PuzzleResultDAO
import sh.zachwal.dailygames.db.dao.game.Top5DAO
import sh.zachwal.dailygames.db.dao.game.TradleDAO
import sh.zachwal.dailygames.db.dao.game.TravleDAO
import sh.zachwal.dailygames.db.dao.game.WorldleDAO
import sh.zachwal.dailygames.db.jdbi.User
import sh.zachwal.dailygames.db.jdbi.puzzle.Game
import sh.zachwal.dailygames.db.jdbi.puzzle.Puzzle
import sh.zachwal.dailygames.db.jdbi.puzzle.PuzzleResult
import sh.zachwal.dailygames.home.views.ResultFeedItemView
import sh.zachwal.dailygames.results.gameinfo.WorldleInfo
import sh.zachwal.dailygames.users.UserPreferencesService
import sh.zachwal.dailygames.users.UserService
import sh.zachwal.dailygames.utils.DisplayTimeService
import java.time.Clock
import java.time.Duration
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.stream.Stream
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.streams.toList

const val FEED_SIZE = 100
val lookBackWindow: Duration = Duration.ofDays(3)

@Singleton
class ResultService @Inject constructor(
    private val jdbi: Jdbi,
    private val puzzleDAO: PuzzleDAO,
    private val worldleDAO: WorldleDAO,
    private val tradleDAO: TradleDAO,
    private val travleDAO: TravleDAO,
    private val top5DAO: Top5DAO,
    private val flagleDAO: FlagleDAO,
    private val pinpointDAO: PinpointDAO,
    private val geocirclesDAO: GeocirclesDAO,
    private val shareTextParser: ShareTextParser,
    private val userService: UserService,
    private val displayTimeService: DisplayTimeService,
    private val userPreferencesService: UserPreferencesService,
    private val clock: Clock,
) {

    private val logger = LoggerFactory.getLogger(ResultService::class.java)

    fun createResult(
        user: User,
        shareText: String
    ): PuzzleResult {
        // regex & parse share text
        val game = shareTextParser.identifyGame(shareText) ?: run {
            logger.error("Could not recognize $shareText as a valid game")
            throw IllegalArgumentException("Share text could not be recognized as a valid game")
        }

        when (game) {
            Game.WORLDLE -> {
                val parsedResult = shareTextParser.extractWorldleInfo(shareText)
                if (parsedResult.gameInfo !is WorldleInfo) {
                    throw IllegalArgumentException("Parsed result $parsedResult is not a WorldleInfo")
                }
                val puzzle = getOrCreatePuzzle(Puzzle(Game.WORLDLE, parsedResult.puzzleNumber, parsedResult.date))

                return worldleDAO.insertResult(
                    userId = user.id,
                    puzzle = puzzle,
                    score = parsedResult.score,
                    shareText = parsedResult.shareTextNoLink,
                    scorePercentage = parsedResult.gameInfo.percentage,
                )
            }

            Game.TRADLE -> {
                val parsedResult = shareTextParser.extractTradleInfo(shareText)
                val puzzle = getOrCreatePuzzle(Puzzle(Game.TRADLE, parsedResult.puzzleNumber, null))

                return tradleDAO.insertResult(
                    userId = user.id,
                    puzzle = puzzle,
                    score = parsedResult.score,
                    shareText = parsedResult.shareTextNoLink,
                )
            }

            Game.TRAVLE -> {
                val travleInfo = shareTextParser.extractTravleInfo(shareText)
                val puzzle = getOrCreatePuzzle(Puzzle(Game.TRAVLE, travleInfo.puzzleNumber, null))

                return travleDAO.insertResult(
                    userId = user.id,
                    puzzle = puzzle,
                    score = travleInfo.score,
                    shareText = travleInfo.shareTextNoLink,
                    numGuesses = travleInfo.numGuesses,
                    numIncorrect = travleInfo.numIncorrect,
                    numPerfect = travleInfo.numPerfect,
                    numHints = travleInfo.numHints,
                )
            }

            Game.TOP5 -> {
                val top5Info = shareTextParser.extractTop5Info(shareText)
                val puzzle = getOrCreatePuzzle(Puzzle(Game.TOP5, top5Info.puzzleNumber, null))

                return top5DAO.insertResult(
                    userId = user.id,
                    puzzle = puzzle,
                    score = top5Info.score,
                    shareText = top5Info.shareTextNoLink,
                    numGuesses = top5Info.numGuesses,
                    numCorrect = top5Info.numCorrect,
                    isPerfect = top5Info.isPerfect,
                )
            }

            Game.FLAGLE -> {
                val flagleInfo = shareTextParser.extractFlagleInfo(shareText)
                val puzzle = getOrCreatePuzzle(Puzzle(Game.FLAGLE, flagleInfo.puzzleNumber, flagleInfo.date))

                return flagleDAO.insertResult(
                    userId = user.id,
                    puzzle = puzzle,
                    score = flagleInfo.score,
                    shareText = flagleInfo.shareTextNoLink,
                )
            }

            Game.PINPOINT -> {
                val pinpointInfo = shareTextParser.extractPinpointInfo(shareText)
                val puzzle = getOrCreatePuzzle(Puzzle(Game.PINPOINT, pinpointInfo.puzzleNumber, null))

                return pinpointDAO.insertResult(
                    userId = user.id,
                    puzzle = puzzle,
                    score = pinpointInfo.score,
                    shareText = pinpointInfo.shareTextNoLink,
                )
            }

            Game.GEOCIRCLES -> {
                val geocirclesInfo = shareTextParser.extractGeocirclesInfo(shareText)
                val puzzle = getOrCreatePuzzle(Puzzle(Game.GEOCIRCLES, geocirclesInfo.puzzleNumber, null))

                return geocirclesDAO.insertResult(
                    userId = user.id,
                    puzzle = puzzle,
                    score = geocirclesInfo.score,
                    shareText = geocirclesInfo.shareTextNoLink,
                )
            }
        }
    }

    private fun getOrCreatePuzzle(puzzle: Puzzle): Puzzle {
        return puzzleDAO.getPuzzle(puzzle.game, puzzle.number) ?: puzzleDAO.insertPuzzle(puzzle)
    }

    fun resultFeed(userId: Long): List<ResultFeedItemView> {
        val results = readRecentResults()
        return results.map { result ->
            val username = userService.getUsernameCached(result.userId)
            ResultFeedItemView(
                username = username ?: "Unknown",
                resultTitle = "${result.game.displayName()} #${result.puzzleNumber}",
                chatHref = chatLink(result.game, result.puzzleNumber),
                shareText = result.shareText,
                timestampText = displayTimeService.displayTime(result.instantSubmitted, userId = userId),
            )
        }
    }

    private fun readRecentResults(): List<PuzzleResult> {
        // Must use JDBI Handle directly to use streaming API
        return jdbi.open().use { handle ->
            val results = puzzleResultDAOs(handle).flatMap { dao ->
                dao.allResultsStream().use {
                    readRecentResults(it)
                }
            }

            results.sortedByDescending { it.instantSubmitted }.take(FEED_SIZE)
        }
    }

    private fun <T : PuzzleResult> readRecentResults(stream: Stream<T>): List<T> {
        return stream
            .takeWhile { it.instantSubmitted.isAfter(Instant.now().minus(lookBackWindow)) }
            .limit(FEED_SIZE.toLong())
            .toList()
    }

    fun allResultsForPuzzle(puzzle: Puzzle): List<PuzzleResult> {
        return when (puzzle.game) {
            Game.WORLDLE -> worldleDAO.resultsForPuzzle(puzzle)
            Game.TRADLE -> tradleDAO.resultsForPuzzle(puzzle)
            Game.TRAVLE -> travleDAO.resultsForPuzzle(puzzle)
            Game.TOP5 -> top5DAO.resultsForPuzzle(puzzle)
            Game.FLAGLE -> flagleDAO.resultsForPuzzle(puzzle)
            Game.PINPOINT -> pinpointDAO.resultsForPuzzle(puzzle)
            Game.GEOCIRCLES -> geocirclesDAO.resultsForPuzzle(puzzle)
        }
    }

    fun resultsForUserToday(user: User): List<PuzzleResult> {
        val userTimeZone = userPreferencesService.getTimeZone(user.id)
        val startOfToday = clock.instant().atZone(userTimeZone).truncatedTo(ChronoUnit.DAYS).toInstant()
        val endOfToday = startOfToday.plus(1, ChronoUnit.DAYS)

        return daos.flatMap { dao ->
            dao.resultsForUserInTimeRange(user.id, startOfToday, endOfToday)
        }
    }

    private val daoClasses = listOf(
        WorldleDAO::class.java,
        TradleDAO::class.java,
        TravleDAO::class.java,
        Top5DAO::class.java,
        FlagleDAO::class.java,
        PinpointDAO::class.java,
        GeocirclesDAO::class.java,
    )

    private val daos = listOf(
        worldleDAO,
        tradleDAO,
        travleDAO,
        top5DAO,
        flagleDAO,
        pinpointDAO,
        geocirclesDAO,
    )

    private fun puzzleResultDAOs(handle: Handle): List<PuzzleResultDAO<*>> {
        return daoClasses.map { handle.attach(it) }
    }
}
