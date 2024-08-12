package sh.zachwal.dailygames.results

import org.jdbi.v3.core.Jdbi
import org.slf4j.LoggerFactory
import sh.zachwal.dailygames.db.dao.game.PuzzleDAO
import sh.zachwal.dailygames.db.dao.game.TradleDAO
import sh.zachwal.dailygames.db.dao.game.TravleDAO
import sh.zachwal.dailygames.db.dao.game.WorldleDAO
import sh.zachwal.dailygames.db.jdbi.User
import sh.zachwal.dailygames.db.jdbi.puzzle.Game
import sh.zachwal.dailygames.db.jdbi.puzzle.Puzzle
import sh.zachwal.dailygames.db.jdbi.puzzle.PuzzleResult
import sh.zachwal.dailygames.home.views.ResultFeedItemView
import sh.zachwal.dailygames.users.UserService
import sh.zachwal.dailygames.utils.toSentenceCase
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.stream.Stream
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.streams.toList

@Singleton
class ResultService @Inject constructor(
    private val jdbi: Jdbi,
    private val puzzleDAO: PuzzleDAO,
    private val worldleDAO: WorldleDAO,
    private val tradleDAO: TradleDAO,
    private val travleDAO: TravleDAO,
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
                "${result.game.name.toSentenceCase()} #${result.puzzleNumber}",
                result.shareText,
            )
        }
    }

    private fun readFirstTwentyResults(): List<PuzzleResult> {
        // Must use JDBI Handle directly to use streaming API
        return jdbi.open().use { handle ->
            val worldleDAO = handle.attach(WorldleDAO::class.java)
            val worldleResults = worldleDAO.allResultsStream().use(::readFirstTwenty)

            val tradleDAO = handle.attach(TradleDAO::class.java)
            val tradleResults = tradleDAO.allResultsStream().use(::readFirstTwenty)

            (worldleResults + tradleResults).sortedByDescending { it.instantSubmitted }.take(20)
        }
    }

    private fun <T : PuzzleResult> readFirstTwenty(stream: Stream<T>): List<T> {
        return stream
            .takeWhile { it.instantSubmitted.isAfter(Instant.now().minus(2, ChronoUnit.DAYS)) }
            .limit(20)
            .toList()
    }
}
