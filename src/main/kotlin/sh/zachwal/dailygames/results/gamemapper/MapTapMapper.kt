package sh.zachwal.dailygames.results.gamemapper

import sh.zachwal.dailygames.db.jdbi.User
import sh.zachwal.dailygames.db.jdbi.puzzle.Game
import sh.zachwal.dailygames.db.jdbi.puzzle.PuzzleResult
import sh.zachwal.dailygames.results.resultinfo.ParsedResult
import sh.zachwal.dailygames.users.UserPreferencesService
import java.time.Clock
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MapTapMapper @Inject constructor(
    private val clock: Clock,
    private val userPreferencesService: UserPreferencesService,
) : GameMapper {
    override val game = Game.MAPTAP

    private val firstLineRegex = Regex("""^(?:www\.)?maptap\.gg .+$""")

    override fun matches(shareText: String): Boolean {
        val firstLine = shareText.trim().lines().firstOrNull() ?: return false
        return firstLineRegex.matches(firstLine)
    }

    override fun extract(shareText: String, user: User): ParsedResult {
        TODO()
    }

    override fun shareLine(result: PuzzleResult): String {
        TODO()
    }
}
