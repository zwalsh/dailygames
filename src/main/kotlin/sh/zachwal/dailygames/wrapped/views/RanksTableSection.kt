package sh.zachwal.dailygames.wrapped.views

import kotlinx.html.DIV
import kotlinx.html.TBODY
import kotlinx.html.ThScope
import kotlinx.html.h1
import kotlinx.html.h6
import kotlinx.html.table
import kotlinx.html.tbody
import kotlinx.html.td
import kotlinx.html.th
import kotlinx.html.thead
import kotlinx.html.tr
import sh.zachwal.dailygames.db.jdbi.puzzle.Game
import sh.zachwal.dailygames.shared_html.HTMLView

data class RanksTableSection(
    val title: String,
    val heading: String,
    val subHeading: String?,
    val rows: List<RanksTableRowView>
) : WrappedSection(
    height = "", // Don't force view height = 90
) {
    override fun DIV.content() {
        h1 { +title }
        subHeading?.let {
            h6(classes = "text-secondary fst-italic") {
                +it
            }
        }

        table(classes = "table") {
            thead {
                tr {
                    th(scope = ThScope.col) { }
                    th(scope = ThScope.col) { +heading }
                    th(scope = ThScope.col) { +"Rank" }
                }
            }
            tbody {
                rows.forEach { row ->
                    row.renderIn(this)
                }
            }
        }
    }

    override val classes = "align-items-center ranks-table-section"
}

data class RanksTableRowView(
    val game: Game,
    val stat: Int,
    val rank: Int,
) : HTMLView<TBODY>() {

    private val rankText = when (rank) {
        1 -> "🥇${game.perfectEmoji()}"
        2 -> "🥈"
        3 -> "🥉"
        else -> "$rank"
    }

    override fun TBODY.render() {
        tr {
            th(scope = ThScope.row) { +"${game.emoji()} ${game.displayName()}" }
            td { +"$stat" }
            td { +rankText }
        }
    }
}
