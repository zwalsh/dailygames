package sh.zachwal.dailygames.results

import sh.zachwal.dailygames.db.jdbi.User
import sh.zachwal.dailygames.results.gamemapper.GameMapper
import sh.zachwal.dailygames.results.resultinfo.ParsedResult
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ShareTextParser @Inject constructor(
    private val mappers: Set<@JvmSuppressWildcards GameMapper>,
) {

    fun parse(shareText: String, user: User): ParsedResult {
        val mapper = mappers.firstOrNull { it.matches(shareText) }
            ?: throw UnrecognizedShareTextException("Share text could not be recognized as a valid game")
        return mapper.extract(shareText, user)
    }
}
