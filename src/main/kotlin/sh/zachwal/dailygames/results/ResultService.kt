package sh.zachwal.dailygames.results

import org.jdbi.v3.core.Jdbi
import org.slf4j.LoggerFactory
import sh.zachwal.dailygames.db.dao.game.FlagleDAO
import sh.zachwal.dailygames.db.dao.game.PuzzleDAO
import sh.zachwal.dailygames.db.dao.game.Top5DAO
import sh.zachwal.dailygames.db.dao.game.TradleDAO
import sh.zachwal.dailygames.db.dao.game.TravleDAO
import sh.zachwal.dailygames.db.dao.game.WorldleDAO
import sh.zachwal.dailygames.db.jdbi.User
import sh.zachwal.dailygames.db.jdbi.puzzle.Game
import sh.zachwal.dailygames.db.jdbi.puzzle.Puzzle
import sh.zachwal.dailygames.db.jdbi.puzzle.PuzzleResult
import sh.zachwal.dailygames.home.views.ResultFeedItemView
import sh.zachwal.dailygames.users.UserService
import java.text.SimpleDateFormat
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.temporal.ChronoUnit
import java.util.Date
import java.util.stream.Stream
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.streams.toList

const val FEED_SIZE = 30

@Singleton
class ResultService @Inject constructor(
    private val jdbi: Jdbi,
    private val puzzleDAO: PuzzleDAO,
    private val worldleDAO: WorldleDAO,
    private val tradleDAO: TradleDAO,
    private val travleDAO: TravleDAO,
    private val top5DAO: Top5DAO,
    private val flagleDAO: FlagleDAO,
    private val shareTextParser: ShareTextParser,
    private val userService: UserService,
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
                val worldleInfo = shareTextParser.extractWorldleInfo(shareText)
                val puzzle = getOrCreatePuzzle(Puzzle(Game.WORLDLE, worldleInfo.puzzleNumber, worldleInfo.date))

                return worldleDAO.insertResult(
                    userId = user.id,
                    puzzle = puzzle,
                    score = worldleInfo.score,
                    shareText = worldleInfo.shareTextNoLink,
                    scorePercentage = worldleInfo.percentage,
                )
            }

            Game.TRADLE -> {
                val tradleInfo = shareTextParser.extractTradleInfo(shareText)
                val puzzle = getOrCreatePuzzle(Puzzle(Game.TRADLE, tradleInfo.puzzleNumber, null))

                return tradleDAO.insertResult(
                    userId = user.id,
                    puzzle = puzzle,
                    score = tradleInfo.score,
                    shareText = tradleInfo.shareTextNoLink,
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
        }
    }

    private fun getOrCreatePuzzle(puzzle: Puzzle): Puzzle {
        return puzzleDAO.getPuzzle(puzzle.game, puzzle.number) ?: puzzleDAO.insertPuzzle(puzzle)
    }

    fun resultFeed(): List<ResultFeedItemView> {
        val results = readFirstTwentyResults()

        val userNameCache = mutableMapOf<Long, String?>()
        return results.map { result ->
            val username = userNameCache.computeIfAbsent(result.userId) { userService.getUser(it)?.username }
            ResultFeedItemView(
                username ?: "Unknown",
                "${result.game.displayName()} #${result.puzzleNumber}",
                result.shareText,
                displayTime(result.instantSubmitted),
            )
        }
    }

    private fun readFirstTwentyResults(): List<PuzzleResult> {
        // Must use JDBI Handle directly to use streaming API
        return jdbi.open().use { handle ->
            val daos = listOf(
                WorldleDAO::class.java,
                TradleDAO::class.java,
                TravleDAO::class.java,
                Top5DAO::class.java,
                FlagleDAO::class.java,
            ).map { handle.attach(it) }

            val results = daos.flatMap { dao ->
                dao.allResultsStream().use {
                    readFirstTwenty(it)
                }
            }

            results.sortedByDescending { it.instantSubmitted }.take(FEED_SIZE)
        }
    }

    private fun <T : PuzzleResult> readFirstTwenty(stream: Stream<T>): List<T> {
        return stream
            .takeWhile { it.instantSubmitted.isAfter(Instant.now().minus(2, ChronoUnit.DAYS)) }
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
        }
    }
}

fun displayTime(time: Instant, now: Instant = Instant.now()): String {
    val nowDate = LocalDate.ofInstant(now, ZoneId.of("America/New_York"))

    val date = LocalDate.ofInstant(time, ZoneId.of("America/New_York"))
    val diff = now.epochSecond - time.epochSecond

    if (date.equals(nowDate)) {
        return when (diff) {
            in 0..59 -> "Just now"
            in 60..3599 -> "${diff / 60}m ago"
            in 3600..86399 -> "${diff / 3600}h${(diff % 3600) / 60}m ago"
            else -> throw IllegalArgumentException("Time difference is too large")
        }
    } else if (date.equals(nowDate.minusDays(1))) {
        return SimpleDateFormat("'Yesterday at' h:mma 'ET'").format(Date.from(time))
    } else {
        return SimpleDateFormat("EEE MMM d 'at' h:mma 'ET'").format(Date.from(time))
    }
}
