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

data class RanksTableSection(
    val title: String,
) : WrappedSection(
    height = "", // Don't force view height = 90
) {
    override fun DIV.content() {
        h1 { +title }

        table(classes = "table") {
            thead {
                tr {
                    th(scope = ThScope.col) { }
                    th(scope = ThScope.col) { +"Points" }
                    th(scope = ThScope.col) { +"Rank" }
                }
            }
            tbody {
                tr {
                    th(scope = ThScope.row) { +"${Game.WORLDLE.emoji()} Worldle" }
                    td { +"1234" }
                    td { +"1 ${Game.WORLDLE.perfectEmoji()}" }
                }
                tr {
                    th(scope = ThScope.row) { +"${Game.TRADLE.emoji()} Tradle" }
                    td { +"345" }
                    td { +"3" }
                }
                tr {
                    th(scope = ThScope.row) { +"${Game.TRAVLE.emoji()} Travle" }
                    td { +"678" }
                    td { +"2" }
                }
                tr {
                    th(scope = ThScope.row) { +"${Game.TOP5.emoji()} Top 5" }
                    td { +"1234" }
                    td { +"1 ${Game.TOP5.perfectEmoji()}" }
                }
                tr {
                    th(scope = ThScope.row) { +"${Game.FLAGLE.emoji()} Flagle" }
                    td { +"1112" }
                    td { +"5" }
                }
                tr {
                    th(scope = ThScope.row) { +"${Game.PINPOINT.emoji()} Pinpoint" }
                    td { +"1314" }
                    td { +"6" }
                }
                tr {
                    th(scope = ThScope.row) { +"${Game.GEOCIRCLES.emoji()} Geocircles" }
                    td { +"1516" }
                    td { +"7" }
                }
                tr {
                    th(scope = ThScope.row) { +"${Game.FRAMED.emoji()} Framed" }
                    td { +"1718" }
                    td { +"8" }
                }
            }
        }
    }

    override val classes = "align-items-center ranks-table-section"
}
