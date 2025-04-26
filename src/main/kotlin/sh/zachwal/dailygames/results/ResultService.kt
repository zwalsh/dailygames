package sh.zachwal.dailygames.results

import org.jdbi.v3.core.Jdbi
import org.jdbi.v3.core.statement.UnableToExecuteStatementException
import org.jdbi.v3.sqlobject.kotlin.attach
import org.postgresql.util.PSQLException
import org.slf4j.LoggerFactory
import sh.zachwal.dailygames.chat.chatLink
import sh.zachwal.dailygames.db.dao.game.PuzzleDAO
import sh.zachwal.dailygames.db.dao.game.PuzzleResultDAO
import sh.zachwal.dailygames.db.jdbi.User
import sh.zachwal.dailygames.db.jdbi.puzzle.Game
import sh.zachwal.dailygames.db.jdbi.puzzle.Puzzle
import sh.zachwal.dailygames.db.jdbi.puzzle.PuzzleResult
import sh.zachwal.dailygames.home.views.ResultFeedItemView
import sh.zachwal.dailygames.results.resultinfo.ParsedResult
import sh.zachwal.dailygames.users.UserPreferencesService
import sh.zachwal.dailygames.users.UserService
import sh.zachwal.dailygames.utils.DisplayTimeService
import java.time.Clock
import java.time.Duration
import java.time.Instant
import java.time.temporal.ChronoUnit
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.streams.toList

const val FEED_SIZE = 100
val lookBackWindow: Duration = Duration.ofDays(3)

@Singleton
class ResultService @Inject constructor(
    private val jdbi: Jdbi,
    private val puzzleDAO: PuzzleDAO,
    private val resultDAO: PuzzleResultDAO,
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

        val parsedResult = parseResult(shareText, game)
        val puzzle = getOrCreatePuzzle(Puzzle(game, parsedResult.puzzleNumber, parsedResult.date))

        return try {
            // TODO check existing Flagle within time range
            resultDAO.insertResult(
                userId = user.id,
                puzzle = puzzle,
                score = parsedResult.score,
                shareText = parsedResult.shareTextNoLink,
                resultInfo = parsedResult.resultInfo,
            ).also {
                logger.info(
                    "User {} submitted result for {} #{}",
                    user.username, it.game.displayName(), it.puzzleNumber
                )
            }
        } catch (e: UnableToExecuteStatementException) {
            if (e.cause is PSQLException && (e.cause as PSQLException).message?.contains("duplicate key value violates unique constraint") == true) {
                logger.info("User ${user.id} tried to submit a duplicate result for ${game.displayName()} #${parsedResult.puzzleNumber}")
                throw ConflictingPuzzleResultException(puzzle = puzzle, userId = user.id)
            } else {
                logger.error(
                    "Error inserting result for ${game.displayName()} #${parsedResult.puzzleNumber} for user ${user.username}",
                    e
                )
                throw e
            }
        }
    }

    private fun parseResult(shareText: String, game: Game): ParsedResult {
        return when (game) {
            Game.WORLDLE -> shareTextParser.extractWorldleInfo(shareText)
            Game.TRADLE -> shareTextParser.extractTradleInfo(shareText)
            Game.TRAVLE -> shareTextParser.extractTravleInfo(shareText)
            Game.TOP5 -> shareTextParser.extractTop5Info(shareText)
            Game.FLAGLE -> shareTextParser.extractFlagleInfo(shareText)
            Game.PINPOINT -> shareTextParser.extractPinpointInfo(shareText)
            Game.GEOCIRCLES -> shareTextParser.extractGeocirclesInfo(shareText)
            Game.FRAMED -> shareTextParser.extractFramedInfo(shareText)
            Game.GEOGRID -> shareTextParser.extractGeoGridInfo(shareText)
            Game.BANDLE -> shareTextParser.extractBandleInfo(shareText)
            Game.BRACKET_CITY -> shareTextParser.extractBracketCityInfo(shareText)
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
            val dao = handle.attach<PuzzleResultDAO>()
            dao.allResultsStream()
                .takeWhile { it.instantSubmitted.isAfter(Instant.now().minus(lookBackWindow)) }
                .limit(FEED_SIZE.toLong())
                .toList()
        }
    }

    fun allResultsForPuzzle(puzzle: Puzzle): List<PuzzleResult> {
        return resultDAO.resultsForPuzzle(puzzle)
    }

    fun resultsForUserToday(user: User): List<PuzzleResult> {
        val userTimeZone = userPreferencesService.getTimeZone(user.id)
        val startOfToday = clock.instant().atZone(userTimeZone).truncatedTo(ChronoUnit.DAYS).toInstant()
        val endOfToday = startOfToday.plus(1, ChronoUnit.DAYS)

        return resultDAO.resultsForUserInTimeRange(user.id, startOfToday, endOfToday)
    }

    fun anyResultsToday(user: User): Boolean {
        val userTimeZone = userPreferencesService.getTimeZone(user.id)
        val startOfToday = clock.instant().atZone(userTimeZone).truncatedTo(ChronoUnit.DAYS).toInstant()
        val endOfToday = startOfToday.plus(1, ChronoUnit.DAYS)

        return jdbi.open().use { handle ->
            val resultDAO = handle.attach<PuzzleResultDAO>()
            resultDAO.allResultsBetweenStream(startOfToday, endOfToday)
                .findAny()
                .isPresent
        }
    }

    fun resultCountByGame(since: Instant, excludeUserId: Long): Map<Game, Int> {
        return resultDAO.countByGameSinceExcludingUser(
            since = since,
            userId = excludeUserId,
        )
    }
}
