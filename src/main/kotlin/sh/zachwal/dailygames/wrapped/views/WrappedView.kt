package sh.zachwal.dailygames.wrapped.views

import kotlinx.html.HTML
import kotlinx.html.body
import kotlinx.html.div
import kotlinx.html.head
import kotlinx.html.link
import kotlinx.html.script
import kotlinx.html.title
import sh.zachwal.dailygames.nav.NavView
import sh.zachwal.dailygames.shared_html.HTMLView
import sh.zachwal.dailygames.shared_html.darkMode
import sh.zachwal.dailygames.shared_html.headSetup

data class WrappedView(
    val name: String,
    val year: Int,
    val navView: NavView,
    val sections: List<WrappedSection>,
) : HTMLView<HTML>() {
    override fun HTML.render() {
        head {
            title {
                +"$name's $year Wrapped"
            }
            headSetup()
            link(href = "/static/src/css/wrapped.css", rel = "stylesheet")
            script {
                src = "/static/src/js/wrapped.js"
            }
        }
        body {
            darkMode()
            div(classes = "container mb-4") {
                sections.forEach {
                    it.renderIn(this@div)
                }
            }
        }
    }
}
