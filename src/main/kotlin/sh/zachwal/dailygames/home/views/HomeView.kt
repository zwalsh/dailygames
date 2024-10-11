package sh.zachwal.dailygames.home.views

import kotlinx.html.FormMethod.post
import kotlinx.html.HTML
import kotlinx.html.body
import kotlinx.html.div
import kotlinx.html.form
import kotlinx.html.h1
import kotlinx.html.head
import kotlinx.html.id
import kotlinx.html.script
import kotlinx.html.submitInput
import kotlinx.html.textArea
import kotlinx.html.title
import sh.zachwal.dailygames.nav.NavView
import sh.zachwal.dailygames.shared_html.HTMLView
import sh.zachwal.dailygames.shared_html.darkMode
import sh.zachwal.dailygames.shared_html.headSetup
import sh.zachwal.dailygames.shared_html.jquery

const val SHARE_TEXT_ID = "shareTextId"

data class HomeView(
    val resultFeed: List<ResultFeedItemView>,
    val shareTextModalView: ShareTextModalView?,
    val nav: NavView,
) : HTMLView<HTML>() {

    override fun HTML.render() {
        head {
            title("Daily Games")
            headSetup()
            jquery()
        }
        body {
            darkMode()
            nav.renderIn(this)
            shareTextModalView?.renderIn(this)
            div(classes = "container") {
                div(classes = "row") {
                    div(classes = "col") {
                        div(classes = "card mx-3") {
                            div(classes = "card-body bg-secondary-subtle") {
                                h1(classes = "card-title text-center") {
                                    +"Submit Game"
                                }
                                form(method = post) {
                                    div(classes = "mb-3") {
                                        textArea(classes = "form-control bg-dark-subtle", rows = "5") {
                                            id = SHARE_TEXT_ID
                                            name = SHARE_TEXT_ID
                                            placeholder = "e.g. #Worldle #123 (10.08.2024) 4/6 (100%)..."
                                        }
                                    }
                                    div(classes = "d-flex justify-content-end") {
                                        submitInput(classes = "btn btn-success") {
                                            value = "Submit"
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                // TODO add button to re-pop modal

                GameLinkView.renderIn(this)

                div(classes = "row border-top") {
                    div(classes = "col") {
                        h1(classes = "text-center mt-2") {
                            +"Feed"
                        }
                    }
                }
                div(classes = "row") {
                    resultFeed.forEach {
                        it.renderIn(this)
                    }
                }
            }
            script {
                src = "/static/src/js/home.js"
            }
        }
    }
}
