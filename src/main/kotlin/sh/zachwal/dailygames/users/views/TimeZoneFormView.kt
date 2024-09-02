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
import java.time.ZoneId

class TimeZoneFormView(
    val currentTimeZone: ZoneId,
    val popularTimeZones: List<ZoneId>,
    val timeZonesToNames: Map<ZoneId, String>,
) : HTMLView<DIV>() {
    override fun DIV.render() {

        card(cardHeader = "Set Time Zone", cardHeaderClasses = "text-center") {
            form(method = post, action = POST_TIME_ZONE_ROUTE) {
                div(classes = "mb-3") {
                    select(classes = "form-select") {
                        id = TIME_ZONE_FORM_PARAM
                        name = TIME_ZONE_FORM_PARAM

                        // First set current choice
                        option {
                            value = currentTimeZone.id
                            selected = true
                            +timeZonesToNames[currentTimeZone]!!
                        }

                        // Then add a separator line
                        option {
                            disabled = true
                            +"──────"
                        }

                        // Then show popular ones
                        popularTimeZones
                            .minus(currentTimeZone)
                            .forEach {
                                option {
                                    value = it.id
                                    +timeZonesToNames[it]!!
                                }
                            }

                        // Then add a separator line
                        option {
                            disabled = true
                            +"──────"
                        }

                        // Then display the rest
                        timeZonesToNames
                            .filterKeys { it != currentTimeZone }
                            .filterKeys { it !in popularTimeZones }
                            .forEach { (zoneId, name) ->
                                option {
                                    value = zoneId.id
                                    +name
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
