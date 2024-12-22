package sh.zachwal.dailygames.wrapped.views

import kotlinx.html.DIV
import kotlinx.html.ThScope
import kotlinx.html.h1
import kotlinx.html.table
import kotlinx.html.tbody
import kotlinx.html.td
import kotlinx.html.th
import kotlinx.html.tr
import sh.zachwal.dailygames.db.jdbi.WrappedInfo
import java.time.format.DateTimeFormatter

private val bestDayFormatter = DateTimeFormatter.ofPattern("M/d/yy")

data class SummaryTableSection(
    val wrappedInfo: WrappedInfo,
    val wrappedIndex: Int,
) : WrappedSection(
    height = "", // Don't force view height = 90
    wrappedIndex = wrappedIndex,
) {

    private val rankString = when (wrappedInfo.totalPointsRank) {
        1 -> "\uD83E\uDD47"
        2 -> "\uD83E\uDD48"
        3 -> "\uD83E\uDD49"
        else -> "${wrappedInfo.totalPointsRank}"
    }

    private val favoriteGameString = wrappedInfo.favoriteGame.let { game ->
        "${game.emoji()}${game.displayName()}${game.emoji()} "
    }

    private val bestDayString = wrappedInfo.bestDay?.let { day ->
        "${bestDayFormatter.format(day)} - ${wrappedInfo.bestDayPoints} points"
    }

    private val bestGameString = wrappedInfo.bestGame?.let { game ->
        "${game.emoji()} ${game.displayName()} - #${wrappedInfo.ranksPerGameAverage[game]}"
    }

    override fun DIV.content() {
        h1 { +"Summary" }

        table(classes = "table") {
            tbody {
                tr {
                    th(scope = ThScope.row) { +"Total Games Played" }
                    td { +wrappedInfo.totalGamesPlayed.toString() }
                }
                tr {
                    th(scope = ThScope.row) { +"Total Points" }
                    td { +wrappedInfo.totalPoints.toString() }
                }
                tr {
                    th(scope = ThScope.row) { +"Overall Rank" }
                    td { +rankString } // 🥇🥈🥉
                }
                tr {
                    th(scope = ThScope.row) { +"Favorite Game" }
                    td { +favoriteGameString }
                }
                bestDayString?.let {
                    tr {
                        th(scope = ThScope.row) { +"Best Day" }
                        td { +bestDayString }
                    }
                }
                tr {
                    th(scope = ThScope.row) { +"Minutes Played" }
                    td { +wrappedInfo.totalMinutes.toString() }
                }
                bestGameString?.let {
                    tr {
                        th(scope = ThScope.row) { +"Best Game" }
                        td { +bestGameString }
                    }
                }
            }
        }
    }

    override val classes = "align-items-center ranks-table-section"
}
