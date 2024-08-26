package sh.zachwal.dailygames.home.views

import kotlinx.html.FormMethod.post
import kotlinx.html.HTML
import kotlinx.html.body
import kotlinx.html.div
import kotlinx.html.form
import kotlinx.html.h1
import kotlinx.html.head
import kotlinx.html.id
import kotlinx.html.submitInput
import kotlinx.html.textArea
import kotlinx.html.title
import sh.zachwal.dailygames.nav.NavItem
import sh.zachwal.dailygames.nav.NavView
import sh.zachwal.dailygames.shared_html.HTMLView
import sh.zachwal.dailygames.shared_html.card
import sh.zachwal.dailygames.shared_html.darkMode
import sh.zachwal.dailygames.shared_html.headSetup

const val SHARE_TEXT_ID = "shareTextId"

data class HomeView constructor(
    val username: String,
    val resultFeed: List<ResultFeedItemView>,
) : HTMLView<HTML>() {

    private val nav = NavView(username = username, currentActiveNavItem = NavItem.HOME)

    override fun HTML.render() {
        head {
            title("Daily Games")
            headSetup()
        }
        body {
            darkMode()
            nav.renderIn(this)
            div(classes = "container") {
                div(classes = "row") {
                    div(classes = "col") {
                        card(cardTitle = "Submit Game", cardTitleClasses = "text-center", classes = "mx-3") {
                            form(method = post) {
                                div(classes = "mb-3") {
                                    textArea(classes = "form-control", rows = "7") {
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
// Remove for now, add later with expander maybe?
//                div(classes = "row") {
//                    div(classes = "col") {
//                        card(cardTitle = "Games") {
//                            ul {
//                                li {
//                                    a(href = "https://worldle.teuteuf.fr/") { +"Worldle" }
//                                }
//                            }
//                        }
//                    }
//                }
                div(classes = "row mt-4 border-top") {
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
        }
    }
}
