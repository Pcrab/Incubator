package xyz.pcrab.plugins

import com.auth0.jwk.JwkProvider
import io.ktor.routing.*
import io.ktor.application.*
import io.ktor.response.*
import xyz.pcrab.models.SecretObject
import xyz.pcrab.routes.registerIncubatorRoutes
import xyz.pcrab.routes.registerUserRoutes

fun Application.configureRouting(secretObject: SecretObject, jwkProvider: JwkProvider) {
    routing {
        get("/") {
            call.respondText("Hello World!")
        }
    }
    registerIncubatorRoutes()
    registerUserRoutes(secretObject, jwkProvider)
}
