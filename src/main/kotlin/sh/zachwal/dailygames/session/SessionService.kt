package sh.zachwal.dailygames.session

import io.ktor.application.ApplicationCall
import io.ktor.sessions.sessions
import sh.zachwal.dailygames.session.principals.UserSessionPrincipal
import java.time.Instant
import java.time.temporal.ChronoUnit

const val USER_SESSION = "USER_SESSION"
const val CONTACT_SESSION = "CONTACT_SESSION"

class SessionService {

    fun createUserSession(call: ApplicationCall, username: String) {
        val oneWeekAway = Instant.now().plus(7, ChronoUnit.DAYS).toEpochMilli()
        call.sessions.set(USER_SESSION, UserSessionPrincipal(username, oneWeekAway))
    }
}