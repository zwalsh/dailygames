package sh.zachwal.dailygames.utils

import io.ktor.application.ApplicationCall
import io.ktor.features.origin
import io.ktor.features.toLogString
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.request.ApplicationRequest
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
fun defaultFormat(call: ApplicationCall): String =
    when (val status = call.response.status() ?: "Unhandled") {
        HttpStatusCode.Found -> "$status: ${call.request.toLogString()} -> ${call.response.headers[HttpHeaders.Location]}"
        "Unhandled" -> "$status: ${call.request.toLogString()}"
        else -> "$status: ${call.request.toLogString()}"
    }
