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
import sh.zachwal.dailygames.shared_html.HTMLView
import sh.zachwal.dailygames.shared_html.headSetup

object AdminPageView : HTMLView<HTML>() {
    override fun HTML.render() {
        head {
            title {
                +"Admin"
            }
            headSetup()
        }
        body {
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
                }
            }
        }
    }
}
