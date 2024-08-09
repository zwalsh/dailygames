package sh.zachwal.dailygames.users.views

import kotlinx.html.HTML
import kotlinx.html.a
import kotlinx.html.body
import kotlinx.html.div
import kotlinx.html.h1
import kotlinx.html.head
import kotlinx.html.li
import kotlinx.html.title
import kotlinx.html.ul
import sh.zachwal.dailygames.shared_html.HTMLView
import sh.zachwal.dailygames.shared_html.darkMode
import sh.zachwal.dailygames.shared_html.headSetup

data class ProfileView(
    private val greeting: String,
    private val username: String,
    private val isAdmin: Boolean
) :
    HTMLView<HTML>() {
    override fun HTML.render() {
        head {
            title {
                +"$username's Profile"
            }
            headSetup()
        }
        body {
            darkMode()
            div(classes = "container") {
                h1 {
                    +"$greeting, $username!"
                }
                ul {
                    if (isAdmin) {
                        li {
                            a(href = "/admin") {
                                +"Admin Page"
                            }
                        }
                    }
                    li {
                        a(href = "/logout") {
                            +"Log out"
                        }
                    }
                }
            }
        }
    }
}
