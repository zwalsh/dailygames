package sh.zachwal.dailygames

import com.fasterxml.jackson.databind.SerializationFeature
import com.google.inject.Guice
import io.ktor.application.Application
import io.ktor.application.install
import io.ktor.application.log
import io.ktor.auth.Authentication
import io.ktor.features.CallLogging
import io.ktor.features.ContentNegotiation
import io.ktor.features.DefaultHeaders
import io.ktor.features.StatusPages
import io.ktor.features.XForwardedHeaderSupport
import io.ktor.http.content.resources
import io.ktor.http.content.static
import io.ktor.jackson.jackson
import io.ktor.routing.routing
import io.ktor.sessions.Sessions
import io.ktor.sessions.cookie
import org.slf4j.event.Level
import sh.zachwal.dailygames.auth.configureFormAuth
import sh.zachwal.dailygames.auth.configureSessionAuth
import sh.zachwal.dailygames.config.AppConfig
import sh.zachwal.dailygames.controller.createControllers
import sh.zachwal.dailygames.features.configureRoleAuthorization
import sh.zachwal.dailygames.features.configureStatusPages
import sh.zachwal.dailygames.guice.ApplicationModule
import sh.zachwal.dailygames.guice.ConfigModule
import sh.zachwal.dailygames.guice.HikariModule
import sh.zachwal.dailygames.guice.JdbiModule
import sh.zachwal.dailygames.roles.RoleAuthorization
import sh.zachwal.dailygames.roles.RoleService
import sh.zachwal.dailygames.session.DbSessionStorage
import sh.zachwal.dailygames.session.SessionCleanupTask
import sh.zachwal.dailygames.session.USER_SESSION
import sh.zachwal.dailygames.session.principals.UserSessionPrincipal
import sh.zachwal.dailygames.users.UserService
import kotlin.collections.set
import kotlin.time.ExperimentalTime

fun main(args: Array<String>): Unit = io.ktor.server.netty.EngineMain.main(args)

@ExperimentalTime
@Suppress("unused") // Referenced in application.conf
@kotlin.jvm.JvmOverloads
fun Application.module(testing: Boolean = false) {

    val injector = Guice.createInjector(
        ApplicationModule(),
        ConfigModule(environment.config),
        JdbiModule(),
        HikariModule(),
    )

    val config = injector.getInstance(AppConfig::class.java)
    log.info("Starting app in ${config.env}")

//    initSentry(config.sentryConfig, config.env)

    val userService = injector.getInstance(UserService::class.java)
    val roleService = injector.getInstance(RoleService::class.java)
    val dbSessionStorage = injector.getInstance(DbSessionStorage::class.java)

    install(CallLogging) {
        level = Level.INFO
    }
    install(DefaultHeaders)

    if (config.env != "DEV") {
        install(XForwardedHeaderSupport)
    }

    install(Sessions) {
        cookie<UserSessionPrincipal>(
            USER_SESSION,
            storage = dbSessionStorage
        ) {
            cookie.httpOnly = true
            cookie.secure = config.env != "DEV"
            cookie.extensions["SameSite"] = "lax"
        }
    }

    install(Authentication) {
        configureFormAuth(userService)
        configureSessionAuth()
    }

    install(RoleAuthorization) {
        configureRoleAuthorization(this, this@module, userService, roleService)
    }

    install(ContentNegotiation) {
        jackson {
            enable(SerializationFeature.INDENT_OUTPUT)
        }
    }

    install(StatusPages) {
        configureStatusPages()
    }

    createControllers(injector)

    routing {
        static("static") {
            resources("static")
        }
    }

    // clean up expired sessions every hour
    val cleanupTask = injector.getInstance(SessionCleanupTask::class.java)
    cleanupTask.repeatCleanup()
}
