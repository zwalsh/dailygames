package sh.zachwal.dailygames.users.views

import kotlinx.html.DIV
import kotlinx.html.FormMethod.post
import kotlinx.html.div
import kotlinx.html.form
import kotlinx.html.id
import kotlinx.html.option
import kotlinx.html.select
import kotlinx.html.submitInput
import sh.zachwal.dailygames.shared_html.HTMLView
import sh.zachwal.dailygames.shared_html.card
import sh.zachwal.dailygames.users.POST_TIME_ZONE_ROUTE
import sh.zachwal.dailygames.users.TIME_ZONE_FORM_PARAM

class TimeZoneFormView(
    val currentTimeZone: String,
    val possibleTimeZones: List<String> = listOf(
        "America/New_York",
        "America/Los_Angeles",
        "Europe/London",
        "Europe/Berlin"
    )
) : HTMLView<DIV>() {
    override fun DIV.render() {
        card(cardHeader = "Set Time Zone", cardHeaderClasses = "text-center") {
            form(method = post, action = POST_TIME_ZONE_ROUTE) {
                div(classes = "mb-3") {
                    select(classes = "form-select") {
                        id = TIME_ZONE_FORM_PARAM
                        name = TIME_ZONE_FORM_PARAM
                        option {
                            value = currentTimeZone
                            selected = true
                            +currentTimeZone
                        }
                        possibleTimeZones.minus(currentTimeZone).forEach {
                            option {
                                value = it
                                +it
                            }
                        }
                    }
                }
                div(classes = "d-flex justify-content-end") {
                    submitInput(classes = "btn btn-primary") {
                        value = "Submit"
                    }
                }
            }
        }
    }
}