package sh.zachwal.dailygames.session

import io.ktor.server.application.ApplicationCall
import io.ktor.server.sessions.sessions
import sh.zachwal.dailygames.session.principals.UserSessionPrincipal
import java.time.Duration
import java.time.Instant
import java.time.temporal.ChronoUnit

const val USER_SESSION = "USER_SESSION"

val SESSION_MAX_DURATION: Duration = Duration.of(30, ChronoUnit.DAYS)

class SessionService {

    fun createUserSession(call: ApplicationCall, username: String) {
        val sessionExpiration = Instant.now().plus(SESSION_MAX_DURATION).toEpochMilli()
        call.sessions.set(USER_SESSION, UserSessionPrincipal(user = username, expiration = sessionExpiration))
    }
}
