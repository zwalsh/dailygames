package sh.zachwal.dailygames.users.views

import kotlinx.html.FormMethod.post
import kotlinx.html.HTML
import kotlinx.html.body
import kotlinx.html.div
import kotlinx.html.form
import kotlinx.html.head
import kotlinx.html.label
import kotlinx.html.passwordInput
import kotlinx.html.submitInput
import kotlinx.html.title
import sh.zachwal.dailygames.shared_html.HTMLView
import sh.zachwal.dailygames.shared_html.card
import sh.zachwal.dailygames.shared_html.darkMode
import sh.zachwal.dailygames.shared_html.headSetup
import sh.zachwal.dailygames.users.CURRENT_PASSWORD_FORM_PARAM
import sh.zachwal.dailygames.users.NEW_PASSWORD_FORM_PARAM
import sh.zachwal.dailygames.users.REPEAT_NEW_PASSWORD_FORM_PARAM

class ChangePasswordView(
    val errorMessage: String? = null
) : HTMLView<HTML>() {
    override fun HTML.render() {
        head {
            title {
                +"Change Password"
            }
            headSetup()
        }
        body {
            darkMode()
            div(classes = "container") {
                div(classes = "row justify-content-center") {
                    div(classes = "col") {
                        errorMessage?.let { message ->
                            div(classes = "alert alert-danger mt-3") {
                                +message
                            }
                        }

                        card(cardTitle = "Change Password") {
                            form(method = post, classes = "mb-3") {
                                div(classes = "mb-3") {
                                    label { +"Current Password" }
                                    passwordInput(
                                        name = CURRENT_PASSWORD_FORM_PARAM,
                                        classes = "form-control"
                                    )
                                }
                                div(classes = "mb-3") {
                                    label { +"New Password" }
                                    passwordInput(
                                        name = NEW_PASSWORD_FORM_PARAM,
                                        classes = "form-control"
                                    )
                                }
                                div(classes = "mb-3") {
                                    label { +"Repeat New Password" }
                                    passwordInput(
                                        name = REPEAT_NEW_PASSWORD_FORM_PARAM,
                                        classes = "form-control"
                                    )
                                }
                                submitInput(classes = "btn btn-primary") {
                                    value = "Submit"
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
