package sh.zachwal.dailygames.home.views

import kotlinx.html.FormMethod.post
import kotlinx.html.HTML
import kotlinx.html.a
import kotlinx.html.body
import kotlinx.html.div
import kotlinx.html.form
import kotlinx.html.h1
import kotlinx.html.head
import kotlinx.html.id
import kotlinx.html.label
import kotlinx.html.li
import kotlinx.html.p
import kotlinx.html.passwordInput
import kotlinx.html.style
import kotlinx.html.submitInput
import kotlinx.html.textArea
import kotlinx.html.textInput
import kotlinx.html.title
import kotlinx.html.ul
import sh.zachwal.dailygames.nav.NavItem
import sh.zachwal.dailygames.nav.NavView
import sh.zachwal.dailygames.shared_html.HTMLView
import sh.zachwal.dailygames.shared_html.card
import sh.zachwal.dailygames.shared_html.darkMode
import sh.zachwal.dailygames.shared_html.headSetup

const val SHARE_TEXT_ID = "shareTextId"

data class HomeView(
    val username: String
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
                            form(method = post, classes = "mb-3") {
                                div(classes = "mb-3") {
                                    textArea(classes = "form-control", rows = "9") {
                                        id = SHARE_TEXT_ID
                                        name = SHARE_TEXT_ID
                                        placeholder = "e.g. #Worldle #123 (10.08.2024) 4/6 (100%)..."
                                    }
                                }
                                submitInput(classes = "btn btn-primary") {
                                    value = "Submit"
                                }
                            }
                        }
                    }
                }
                div(classes = "row") {
                    div(classes = "col") {
                        card(cardTitle = "Games") {
                            ul {
                                li {
                                    a(href = "https://worldle.teuteuf.fr/") { +"Worldle" }
                                }
                            }
                        }
                    }
                }
                div(classes = "row mt-4 border-top") {
                    div(classes = "col") {
                        h1(classes = "text-center mt-2 text-decoration-underline") {
                            +"Feed"
                        }
                    }
                }
                div(classes = "row") {
                    div(classes = "col") {
                        card(
                            cardHeader = "zach's #Worldle #934",
                            cardHeaderClasses = "fs-5",
                            classes = "mx-3 mt-2 mb-4"
                        ) {
                            p {
                                style = "white-space: pre-wrap;"
                                +"""
                                #Worldle #934 (12.08.2024) 4/6 (100%)
                                🟩🟩🟩🟩🟨⬅️
                                🟩🟩🟩🟩🟨⬅️
                                🟩🟩🟩🟩🟨↗️
                                🟩🟩🟩🟩🟩🎉
                            """.trimIndent()
                            }
                        }
                    }
                }
            }
        }
    }
}
