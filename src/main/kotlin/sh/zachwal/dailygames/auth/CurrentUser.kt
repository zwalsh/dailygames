package sh.zachwal.dailygames.auth

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.ApplicationCall
import io.ktor.server.response.respond
import io.ktor.server.sessions.get
import io.ktor.server.sessions.sessions
import sh.zachwal.dailygames.db.jdbi.User
import sh.zachwal.dailygames.session.principals.UserSessionPrincipal
import sh.zachwal.dailygames.users.UserService

suspend fun currentUser(call: ApplicationCall, userService: UserService): User {
    return userOrNull(call, userService) ?: run {
        call.respond(HttpStatusCode.Unauthorized)
        throw UnauthorizedException()
    }
}

fun userOrNull(call: ApplicationCall, userService: UserService): User? {
    val p = call.sessions.get<UserSessionPrincipal>()
    return p?.let { userService.getUser(p.user) }
}

class UnauthorizedException : Exception()
