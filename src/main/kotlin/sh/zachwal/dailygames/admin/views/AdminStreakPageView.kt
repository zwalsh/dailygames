package sh.zachwal.dailygames.admin.views

import kotlinx.html.HTML
import kotlinx.html.body
import kotlinx.html.div
import kotlinx.html.h1
import kotlinx.html.head
import kotlinx.html.table
import kotlinx.html.td
import kotlinx.html.th
import kotlinx.html.title
import kotlinx.html.tr
import sh.zachwal.dailygames.db.jdbi.User
import sh.zachwal.dailygames.nav.NavView
import sh.zachwal.dailygames.shared_html.HTMLView
import sh.zachwal.dailygames.shared_html.darkMode
import sh.zachwal.dailygames.shared_html.headSetup

data class AdminStreakPageView(
    val userStreaks: Map<User, Int>,
    val navView: NavView,
) : HTMLView<HTML>() {
    override fun HTML.render() {
        head {
            title {
                +"Admin - Streaks"
            }
            headSetup()
        }
        body {
            darkMode()
            navView.renderIn(this)
            div(classes = "container") {
                div(classes = "row justify-content-center") {
                    h1(classes = "text-center") {
                        +"Streaks"
                    }
                    table(classes = "table") {
                        tr {
                            th {
                                +"User"
                            }
                            th {
                                +"Streak"
                            }
                        }
                        userStreaks
                            .entries
                            .sortedByDescending { it.value }
                            .forEach { (user, streak) ->
                                tr {
                                    th {
                                        +user.username
                                    }
                                    td {
                                        +streak.toString()
                                    }
                                }
                            }
                    }
                }
            }
        }
    }
}
