package sh.zachwal.dailygames.wrapped.views

import kotlinx.html.DIV
import kotlinx.html.ThScope
import kotlinx.html.h1
import kotlinx.html.table
import kotlinx.html.tbody
import kotlinx.html.td
import kotlinx.html.th
import kotlinx.html.thead
import kotlinx.html.tr
import sh.zachwal.dailygames.db.jdbi.puzzle.Game

data class SummaryTableSection(
    val f: String,
) : WrappedSection(
    height = "", // Don't force view height = 90
) {
    override fun DIV.content() {
        h1 { +"Summary" }

        table(classes = "table") {
            tbody {
                tr {
                    th(scope = ThScope.row) { +"Total Games Played" }
                    td { +"982" }
                }
                tr {
                    th(scope = ThScope.row) { +"Total Points" }
                    td { +"5487" }
                }
                tr {
                    th(scope = ThScope.row) { +"Overall Rank" }
                    td { +"1 \uD83E\uDD47" } // 🥇🥈🥉
                }
                tr {
                    th(scope = ThScope.row) { +"Favorite Game" }
                    td { +"${Game.TRAVLE.emoji()}Travle${Game.TRAVLE.emoji()} " }
                }
                tr {
                    th(scope = ThScope.row) { +"Best Day" }
                    td { +"9/14/24 - 48 points" }
                }
                tr {
                    th(scope = ThScope.row) { +"Minutes Played" }
                    td { +"1112" }
                }
                tr {
                    th(scope = ThScope.row) { +"Best Game" }
                    td { +"${Game.WORLDLE.emoji()} Worldle - 3rd \uD83E\uDD49" }
                }
            }
        }
    }

    override val classes = "align-items-center ranks-table-section"
}
