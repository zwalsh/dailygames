package sh.zachwal.dailygames.utils

import io.ktor.features.origin
import io.ktor.request.ApplicationRequest

fun ApplicationRequest.remote(): String {
    val clientHost = origin.remoteHost
    val clientPort = origin.port
    return "$clientHost:$clientPort"
}
