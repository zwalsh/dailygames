package sh.zachwal.dailygames.users.views

import kotlinx.html.FormMethod.post
import kotlinx.html.HTML
import kotlinx.html.a
import kotlinx.html.body
import kotlinx.html.div
import kotlinx.html.form
import kotlinx.html.head
import kotlinx.html.label
import kotlinx.html.passwordInput
import kotlinx.html.submitInput
import kotlinx.html.textInput
import kotlinx.html.title
import sh.zachwal.dailygames.shared_html.HTMLView
import sh.zachwal.dailygames.shared_html.card
import sh.zachwal.dailygames.shared_html.darkMode
import sh.zachwal.dailygames.shared_html.headSetup

data class LoginView(private val failed: Boolean) : HTMLView<HTML>() {
    override fun HTML.render() {
        head {
            title {
                +"Login"
            }
            headSetup()
        }
        body {
            darkMode()
            div(classes = "container") {
                div(classes = "row justify-content-center") {
                    div(classes = "col") {
                        card(cardTitle = "Login") {
                            form(method = post, classes = "mb-1") {
                                div(classes = "mb-3") {
                                    label { +"Username" }
                                    textInput(
                                        name = "username",
                                        classes = "form-control"
                                    ) {
                                        placeholder = "user"
                                    }
                                }
                                div(classes = "mb-3") {
                                    label { +"Password" }
                                    passwordInput(
                                        name = "password",
                                        classes = "form-control"
                                    ) {
                                        placeholder = "password"
                                    }
                                }
                                if (failed) {
                                    div(classes = "alert alert-danger") {
                                        +"Login attempt failed"
                                    }
                                }
                                submitInput(classes = "btn btn-primary") {
                                    value = "Log in"
                                }
                            }
                            a(href = "/register") {
                                +"Register"
                            }
                        }
                    }
                }
            }
        }
    }
}
