package sh.zachwal.dailygames.home.views

import kotlinx.html.HTML
import kotlinx.html.a
import kotlinx.html.body
import kotlinx.html.div
import kotlinx.html.h1
import kotlinx.html.head
import kotlinx.html.i
import kotlinx.html.p
import kotlinx.html.title
import sh.zachwal.dailygames.shared_html.HTMLView
import sh.zachwal.dailygames.shared_html.darkMode
import sh.zachwal.dailygames.shared_html.headSetup

object HeroView : HTMLView<HTML>() {
    override fun HTML.render() {
        head {
            title("Daily Games")
            headSetup()
        }
        body {
            darkMode()
            div(classes = "row align-items-center vh-100 vw-100") {
                div(classes = "px-4 py-5 my-5 text-center") {
                    i(classes = "bi bi-globe-europe-africa fs-1") // TODO better icon
                    h1(classes = "display-5 fw-bold text-body-emphasis") {
                        +"Daily Games"
                    }
                    div(classes = "col-lg-6 mx-auto") {
                        p(classes = "lead mb-4") {
                            +"Track & share your performance in daily games like "
                            a(href = "https://worldle.teuteuf.fr/") { +"Worldle" }
                            +", "
                            a(href = "https://games.oec.world/en/tradle/") { +"Tradle" }
                            +", "
                            a(href = "https://top5-game.com/") { +"Top 5" }
                            +", and more."
                        }
                        div(classes = "d-grid gap-2 d-sm-flex justify-content-sm-center px-5") {
                            a(href = "/register", classes = "btn btn-primary") { +"Register" }
                            a(href = "/login", classes = "btn btn-secondary") { +"Login" }
                        }
                    }
                }
            }
        }
    }
}
