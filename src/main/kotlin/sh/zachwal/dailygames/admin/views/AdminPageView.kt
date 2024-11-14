package sh.zachwal.dailygames.admin.views

import kotlinx.html.HTML
import kotlinx.html.a
import kotlinx.html.body
import kotlinx.html.div
import kotlinx.html.h1
import kotlinx.html.h2
import kotlinx.html.head
import kotlinx.html.li
import kotlinx.html.title
import kotlinx.html.ul
import sh.zachwal.dailygames.nav.NavView
import sh.zachwal.dailygames.shared_html.HTMLView
import sh.zachwal.dailygames.shared_html.darkMode
import sh.zachwal.dailygames.shared_html.headSetup

data class AdminPageView constructor(
    private val navView: NavView
) : HTMLView<HTML>() {
    override fun HTML.render() {
        head {
            title {
                +"Admin"
            }
            headSetup()
        }
        body {
            darkMode()
            navView.renderIn(this)
            div(classes = "container") {
                h1 {
                    +"Admin"
                }
                h2 {
                    +"Users"
                }
                ul {
                    li {
                        a(href = "/admin/users") {
                            +"Users"
                        }
                    }
                    li {
                        a(href = "/admin/reset-password") {
                            +"Reset User Password"
                        }
                    }
                    li {
                        a(href = "/admin/backfill") {
                            +"Backfill Result Data"
                        }
                    }
                }
            }
        }
    }
}
