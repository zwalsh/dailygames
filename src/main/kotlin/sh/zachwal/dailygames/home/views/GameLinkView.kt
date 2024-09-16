package sh.zachwal.dailygames.home.views

import kotlinx.html.DIV
import kotlinx.html.button
import kotlinx.html.div
import kotlinx.html.style
import sh.zachwal.dailygames.db.jdbi.puzzle.Game
import sh.zachwal.dailygames.shared_html.HTMLView

object GameLinkView : HTMLView<DIV>() {
    override fun DIV.render() {
        div(classes = "row overflow-auto flex-nowrap py-3 my-1") {
            Game.values().forEach { game ->
                div(classes = "col") {
                    button(classes = "btn btn-secondary") {
                        style = "white-space: nowrap;"
                        +"${game.emoji()} ${game.displayName()}"
                    }
                }
            }
        }
    }
}