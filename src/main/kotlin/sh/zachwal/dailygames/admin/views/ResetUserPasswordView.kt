package sh.zachwal.dailygames.admin.views

import kotlinx.html.FormMethod
import kotlinx.html.HTML
import kotlinx.html.body
import kotlinx.html.div
import kotlinx.html.form
import kotlinx.html.head
import kotlinx.html.label
import kotlinx.html.passwordInput
import kotlinx.html.submitInput
import kotlinx.html.textInput
import kotlinx.html.title
import sh.zachwal.dailygames.admin.NEW_PASSWORD_FORM_PARAM
import sh.zachwal.dailygames.admin.USERNAME_FORM_PARAM
import sh.zachwal.dailygames.nav.NavItem
import sh.zachwal.dailygames.nav.NavView
import sh.zachwal.dailygames.shared_html.HTMLView
import sh.zachwal.dailygames.shared_html.card
import sh.zachwal.dailygames.shared_html.darkMode
import sh.zachwal.dailygames.shared_html.headSetup

class ResetUserPasswordView(
    private val errorMessage: String? = null
) : HTMLView<HTML>() {
    override fun HTML.render() {
        head {
            title {
                +"Reset User Password"
            }
            headSetup()
        }
        body {
            darkMode()
            NavView("", NavItem.PROFILE).renderIn(this)
            div(classes = "container") {
                div(classes = "row justify-content-center") {
                    div(classes = "col") {
                        errorMessage?.let { message ->
                            div(classes = "alert alert-danger mt-3") {
                                +message
                            }
                        }

                        card(cardTitle = "Reset User Password") {
                            form(method = FormMethod.post, classes = "mb-3") {
                                div(classes = "mb-3") {
                                    label { +"Username" }
                                    textInput(
                                        name = USERNAME_FORM_PARAM,
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
                                submitInput(classes = "btn btn-primary") {
                                    value = "Reset Password"
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
