package sh.zachwal.dailygames.chat.views

import kotlinx.html.DIV
import sh.zachwal.dailygames.shared_html.HTMLView

data class AnswerView(
    val answerText: String,
) : HTMLView<DIV>() {
    override fun DIV.render() {
        TODO("Not yet implemented")
    }
}
