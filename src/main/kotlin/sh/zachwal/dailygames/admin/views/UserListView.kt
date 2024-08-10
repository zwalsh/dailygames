package sh.zachwal.dailygames.admin.views

import kotlinx.html.HTML
import kotlinx.html.TBODY
import kotlinx.html.body
import kotlinx.html.div
import kotlinx.html.head
import kotlinx.html.table
import kotlinx.html.tbody
import kotlinx.html.td
import kotlinx.html.th
import kotlinx.html.thead
import kotlinx.html.title
import kotlinx.html.tr
import sh.zachwal.dailygames.shared_html.HTMLView
import sh.zachwal.dailygames.shared_html.card
import sh.zachwal.dailygames.shared_html.darkMode
import sh.zachwal.dailygames.shared_html.headSetup

data class UserListView(
    private val users: List<UserRowView>
) : HTMLView<HTML>() {
    override fun HTML.render() {
        head {
            title { +"Users" }
            headSetup()
        }
        body {
            darkMode()
            div(classes = "container") {
                div(classes = "row") {
                    div(classes = "col") {
                        card(cardHeader = "Users") {
                            table(classes = "table") {
                                thead {
                                    tr {
                                        th {
                                            +"Name"
                                        }
                                        th {
                                            +"Admin"
                                        }
                                    }
                                }
                                tbody {
                                    users.forEach { u ->
                                        u.renderIn(this)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

data class UserRowView(
    private val username: String,
    private val isAdmin: Boolean
) : HTMLView<TBODY>() {
    override fun TBODY.render() {
        tr {
            td {
                +username
            }
            td {
                if (isAdmin) {
                    +"✅"
                } else {
                    +"❌"
                }
            }
        }
    }
}
