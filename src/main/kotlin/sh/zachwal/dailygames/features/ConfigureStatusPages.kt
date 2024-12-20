package sh.zachwal.dailygames.features

import io.ktor.application.call
import io.ktor.features.StatusPages.Configuration
import io.ktor.html.respondHtml
import io.ktor.http.HttpStatusCode
import io.ktor.request.uri
import kotlinx.html.DIV
import kotlinx.html.HTML
import kotlinx.html.body
import kotlinx.html.div
import kotlinx.html.h1
import kotlinx.html.head
import kotlinx.html.p
import kotlinx.html.title
import org.slf4j.LoggerFactory
import sh.zachwal.dailygames.auth.UnauthorizedException
import sh.zachwal.dailygames.shared_html.darkMode
import sh.zachwal.dailygames.shared_html.headSetup

private val logger = LoggerFactory.getLogger("StatusPage")

fun Configuration.configureStatusPages() {

    exception<Exception> {
        logger.error("Unhandled error", it)
        call.respondHtml(HttpStatusCode.InternalServerError) {
            statusPage("Internal Server Error") {
                h1 {
                    +"Error!"
                }
                p {
                    +"${it.message}"
                }
            }
        }
    }

    status(HttpStatusCode.NotFound) {
        call.respondHtml(HttpStatusCode.NotFound) {
            statusPage("${it.value} Not Found") {
                h1 {
                    +"Resource not found"
                }
                p {
                    +" ${it.value}, ${it.description}"
                }
            }
        }
    }

    exception<UnauthorizedException> {
        call.respondHtml(HttpStatusCode.Unauthorized) {
            statusPage("Unauthorized") {
                h1 {
                    +"Unauthorized"
                }
                p {
                    +(
                        "You are not logged in, or do not have the correct permissions, to access " +
                            call.request.uri
                        )
                }
                p {
                    +"${it.message}"
                }
            }
        }
    }

    status(HttpStatusCode.Unauthorized) {
        call.respondHtml(HttpStatusCode.Unauthorized) {
            statusPage("${it.value} Not Logged In") {
                h1 {
                    +"Unauthorized"
                }
                p {
                    +"You must be logged in to access: ${call.request.uri}"
                }
                p {
                    +" ${it.value} ${it.description}"
                }
            }
        }
    }

    status(HttpStatusCode.Forbidden) {
        call.respondHtml(HttpStatusCode.Forbidden) {
            statusPage("${it.value} Access Denied") {
                h1 {
                    +"Access Denied"
                }
                p {
                    +"You do not have permission to access: ${call.request.uri}"
                }
                p {
                    +" ${it.value} ${it.description}"
                }
            }
        }
    }
}

private fun HTML.statusPage(title: String, block: DIV.() -> Unit) {
    head {
        title {
            +title
        }
        headSetup()
    }
    body {
        darkMode()
        div(classes = "container", block = block)
    }
}
