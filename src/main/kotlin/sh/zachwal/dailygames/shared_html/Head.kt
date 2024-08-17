package sh.zachwal.dailygames.shared_html

import kotlinx.html.HEAD
import kotlinx.html.link
import kotlinx.html.meta
import kotlinx.html.script

fun HEAD.bootstrapCss() {
    link(
        rel = "stylesheet",
        href = "https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css",
        type = "text/css"
    )
    link(
        rel = "stylesheet",
        href = "https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.3/font/bootstrap-icons.css",
        type = "text/css"
    )
}

fun HEAD.bootstrapJs() {
    script(
        src = "https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js"
    ) {
        integrity = "sha384-YvpcrYf0tY3lHB60NNkmXc5s9fDVZLESaAA55NDzOxhy9GkcIdslK1eN7N6jIeHz"
        attributes["crossorigin"] = "anonymous"
    }
}

fun HEAD.favicon() {
    // TODO make favicon
    link(href = "/static/favicon.png", rel = "icon", type = "image/png")
}

fun HEAD.mobileUI() {
    meta {
        name = "viewport"
        content = "width=device-width, initial-scale=1, user-scalable=no"
    }
}

fun HEAD.sentryScript() {
    // TODO re-add once sentry project created
//    script(src = "https://js.sentry-cdn.com/$jsDsn.min.js") {
//        attributes["crossorigin"] = "anonymous"
//    }
//    script {
//        unsafe {
//            +"""
//                Sentry.init({
//                  environment: "$jsEnv",
//                });
//            """.trimIndent()
//        }
//    }
}

fun HEAD.headSetup() {
    bootstrapCss()
    bootstrapJs()
    favicon()
    mobileUI()
    sentryScript()
}
