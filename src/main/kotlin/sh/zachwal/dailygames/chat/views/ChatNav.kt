package sh.zachwal.dailygames.chat.views

import kotlinx.html.HEADER
import kotlinx.html.a
import kotlinx.html.div
import kotlinx.html.h1
import kotlinx.html.i
import sh.zachwal.dailygames.db.jdbi.puzzle.Puzzle
import sh.zachwal.dailygames.shared_html.HTMLView

data class ChatNav constructor(
    val prevLink: String? = null,
    val nextLink: String? = null,
    val puzzle: Puzzle,
    val answerView: AnswerView?,
) : HTMLView<HEADER>() {
    override fun HEADER.render() {
        div(classes = "row mx-4 py-2 border-top") {
            div(classes = "col-1 d-flex align-items-center") {
                prevLink?.let { href ->
                    a(href = href, classes = "float-start text-white") {
                        i(classes = "bi bi-chevron-compact-left")
                    }
                }
            }
            div(classes = "col-10") {
                div(classes = "row") {
                    h1(classes = "text-center") {
                        +"${puzzle.game.displayName()} #${puzzle.number}"
                    }
                }
                div(classes = "row") {
                    answerView?.renderIn(this)
                }
            }
            div(classes = "col-1 d-flex align-items-center") {
                nextLink?.let { href ->
                    a(href = href, classes = "float-end text-white") {
                        i(classes = "bi bi-chevron-compact-right")
                    }
                }
            }
        }
    }
}