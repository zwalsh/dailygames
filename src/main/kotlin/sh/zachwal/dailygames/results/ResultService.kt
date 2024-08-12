package sh.zachwal.dailygames.results

import org.jdbi.v3.core.Jdbi
import sh.zachwal.dailygames.db.dao.PuzzleDAO
import sh.zachwal.dailygames.db.dao.WorldleDAO
import sh.zachwal.dailygames.db.jdbi.User
import sh.zachwal.dailygames.db.jdbi.puzzle.Game
import sh.zachwal.dailygames.db.jdbi.puzzle.Puzzle
import sh.zachwal.dailygames.db.jdbi.puzzle.PuzzleResult
import sh.zachwal.dailygames.db.jdbi.puzzle.WorldleResult
import sh.zachwal.dailygames.home.views.ResultFeedItemView
import sh.zachwal.dailygames.users.UserService
import sh.zachwal.dailygames.utils.toSentenceCase
import java.time.Instant
import java.time.temporal.ChronoUnit
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.streams.toList

@Singleton
class ResultService @Inject constructor(
    private val jdbi: Jdbi,
    private val puzzleDAO: PuzzleDAO,
    private val worldleDAO: WorldleDAO,
    private val shareTextParser: ShareTextParser,
    private val userService: UserService,
) {

    fun createResult(
        user: User,
        shareText: String
    ): PuzzleResult {
        // regex & parse share text
        val game = shareTextParser.identifyGame(shareText) ?: run {
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

    private fun readFirstTwentyResults(): List<WorldleResult> {
        // Must use JDBI Handle directly to use streaming API
        return jdbi.open().use { handle ->
            val worldleDAO = handle.attach(WorldleDAO::class.java)
            worldleDAO.allResultsStream().use { results ->
                results
                    .takeWhile { it.instantSubmitted.isAfter(Instant.now().minus(2, ChronoUnit.DAYS)) }
                    .limit(20)
                    .toList()
            }
        }
    }
}
