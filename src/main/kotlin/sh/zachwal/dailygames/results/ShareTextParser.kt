package sh.zachwal.dailygames.results

import sh.zachwal.dailygames.db.jdbi.puzzle.Game
import javax.inject.Singleton

@Singleton
class ShareTextParser {

    val worldleRegex = Regex(
        """#Worldle\s+#(?<puzzleNumber>\d+)\s+\((?<date>\d{2}\.\d{2}\.\d{4})\)\s+(?<score>\S)/6\s+\((?<percentage>\d+)%\)[\s\S]*"""
    )

    fun identifyGame(shareText: String): Game? {
        return when {
            worldleRegex.matches(shareText) -> Game.WORLDLE
            else -> null
        }
    }
}