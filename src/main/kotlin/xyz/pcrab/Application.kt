package xyz.pcrab

import io.ktor.application.*
import io.ktor.features.*
import io.ktor.serialization.*
import xyz.pcrab.plugins.*

fun main(args: Array<String>): Unit =
    io.ktor.server.netty.EngineMain.main(args)

@Suppress("unused") // application.conf references the main function. This annotation prevents the IDE from marking it as unused.
fun Application.module() {

    install(ContentNegotiation) {
        json()
    }
    configureRouting()
    configureMonitoring()
}
