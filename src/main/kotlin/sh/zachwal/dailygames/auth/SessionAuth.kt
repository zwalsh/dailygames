package sh.zachwal.dailygames.auth

import io.ktor.application.call
import io.ktor.auth.Authentication
import io.ktor.auth.session
import io.ktor.response.respondRedirect
import io.sentry.Sentry
import io.sentry.protocol.User
import org.slf4j.LoggerFactory
import sh.zachwal.dailygames.session.principals.UserSessionPrincipal

private val logger = LoggerFactory.getLogger("SessionAuth")

fun Authentication.Configuration.configureSessionAuth() {
    session<UserSessionPrincipal> {
        challenge {
            call.respondRedirect("/login")
        }
        validate { session ->
            val sentryUser = User().apply {
                username = session.user
            }
            Sentry.setUser(sentryUser)
            session.takeIf(UserSessionPrincipal::isValid)
        }
    }
}
