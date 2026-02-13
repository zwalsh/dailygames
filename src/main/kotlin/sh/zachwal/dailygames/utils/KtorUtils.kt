package sh.zachwal.dailygames.utils

import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.ApplicationCall
import io.ktor.server.plugins.origin
import io.ktor.server.request.ApplicationRequest
import io.ktor.server.request.httpMethod
import io.ktor.server.request.uri
import sh.zachwal.dailygames.db.jdbi.puzzle.Game

fun ApplicationRequest.remote(): String {
    val clientHost = origin.remoteHost
    val clientPort = origin.port
    return "$clientHost:$clientPort"
}

fun ApplicationCall.extractGameFromPathParams(): Game? {
    val gameStr = parameters["game"]
    return try {
        gameStr?.let { Game.valueOf(it.uppercase()) }
    } catch (e: IllegalArgumentException) {
        null
    }
}

// Copied from internal implementation in `CallLogging`
fun defaultFormat(call: ApplicationCall): String {
    val requestLog = "${call.request.httpMethod.value} - ${call.request.uri}"
    return when (val status = call.response.status() ?: "Unhandled") {
        HttpStatusCode.Found -> "$status: $requestLog -> ${call.response.headers[HttpHeaders.Location]}"
        "Unhandled" -> "$status: $requestLog"
        else -> "$status: $requestLog"
    }
}
