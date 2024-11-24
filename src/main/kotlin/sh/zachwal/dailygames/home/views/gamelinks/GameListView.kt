package sh.zachwal.dailygames.home.views.gamelinks

import kotlinx.html.DIV
import kotlinx.html.div
import sh.zachwal.dailygames.db.jdbi.puzzle.Game
import sh.zachwal.dailygames.shared_html.HTMLView

data class GameListView(
    val games: List<GameLinkView>
) : HTMLView<DIV>() {
    override fun DIV.render() {
        div(classes = "row overflow-auto flex-nowrap py-3 my-1") {
            games.forEach { game ->
                game.renderIn(this@div)
            }
        }
    }
}
