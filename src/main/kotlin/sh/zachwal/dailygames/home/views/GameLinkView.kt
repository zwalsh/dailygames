package sh.zachwal.dailygames.home.views

import kotlinx.html.DIV
import kotlinx.html.a
import kotlinx.html.div
import kotlinx.html.i
import kotlinx.html.role
import kotlinx.html.style
import sh.zachwal.dailygames.db.jdbi.puzzle.Game
import sh.zachwal.dailygames.shared_html.HTMLView

object GameLinkView : HTMLView<DIV>() {
    override fun DIV.render() {
        div(classes = "row overflow-auto flex-nowrap py-3 my-1") {
            Game.values().forEach { game ->
                div(classes = "col") {
                    a(href = game.href(), classes = "btn btn-secondary", target = "_blank") {
                        role = "button"
                        style = "white-space: nowrap;"
                        +"${game.emoji()} ${game.displayName()}"
                        i(classes = "bi bi-box-arrow-up-right ms-2")
                    }
                }
            }
        }
    }
}
