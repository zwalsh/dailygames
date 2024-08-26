package sh.zachwal.dailygames.utils

import io.ktor.application.ApplicationCall
import io.ktor.features.origin
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