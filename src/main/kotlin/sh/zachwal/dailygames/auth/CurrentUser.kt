package sh.zachwal.dailygames.auth

import io.ktor.application.ApplicationCall
import io.ktor.http.HttpStatusCode
import io.ktor.response.respond
import io.ktor.sessions.get
import io.ktor.sessions.sessions
import sh.zachwal.dailygames.db.jdbi.User
import sh.zachwal.dailygames.session.principals.UserSessionPrincipal
import sh.zachwal.dailygames.users.UserService

suspend fun currentUser(call: ApplicationCall, userService: UserService): User {
    val p = call.sessions.get<UserSessionPrincipal>()
    return p?.let { userService.getUser(p.user) } ?: run {
        call.respond(HttpStatusCode.Unauthorized)
        throw UnauthorizedException()
    }
}

class UnauthorizedException : Exception()
