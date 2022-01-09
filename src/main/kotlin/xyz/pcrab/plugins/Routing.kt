package xyz.pcrab.plugins

import io.ktor.routing.*
import io.ktor.application.*
import io.ktor.response.*
import xyz.pcrab.routes.registerIncubatorRoutes
import xyz.pcrab.routes.registerUserRoutes

fun Application.configureRouting() {
    routing {
        get("/") {
            call.respondText("Hello World!")
        }
    }
    registerIncubatorRoutes()
    registerUserRoutes()
}
