package sh.zachwal.dailygames.home.views

import kotlinx.html.DIV
import sh.zachwal.dailygames.shared_html.HTMLView

data class ResultFeedItemView(
    val username: String,
    val resultTitle: String,
    val shareText: String,
) : HTMLView<DIV>() {
    override fun DIV.render() {
        TODO("Not yet implemented")
    }
}
