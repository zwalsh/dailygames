package sh.zachwal.dailygames.auth

import io.ktor.server.application.call
import io.ktor.server.auth.AuthenticationConfig
import io.ktor.server.auth.session
import io.ktor.server.response.respondRedirect
import io.sentry.Sentry
import io.sentry.protocol.User
import org.slf4j.LoggerFactory
import sh.zachwal.dailygames.session.principals.UserSessionPrincipal

private val logger = LoggerFactory.getLogger("SessionAuth")

fun AuthenticationConfig.configureSessionAuth() {
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
