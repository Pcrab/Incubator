package xyz.pcrab

import io.ktor.application.*
import io.ktor.features.*
import io.ktor.serialization.*
import io.ktor.sessions.*
import xyz.pcrab.models.UserSession
import xyz.pcrab.plugins.*

fun main(args: Array<String>): Unit =
    io.ktor.server.netty.EngineMain.main(args)

@Suppress("unused") // application.conf references the main function. This annotation prevents the IDE from marking it as unused.
fun Application.module() {

    install(ContentNegotiation) {
        json()
    }
    install(CORS) {
        host("127.0.0.1:8000")
        host("localhost:63342")
        allowCredentials = true
    }
    install(Sessions) {
        cookie<UserSession>("user_session", storage = SessionStorageMemory())
    }
    configureRouting()
    configureMonitoring()
}
