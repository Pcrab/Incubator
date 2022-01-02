package xyz.pcrab.routes

import io.ktor.application.*
import io.ktor.response.*
import io.ktor.routing.*
import xyz.pcrab.models.*

fun Route.getUserSessionRoute() {
    get("/user/{username}/{password}") {
        val username = call.parameters["username"]
        val password = call.parameters["password"]
        if (username != null && password != null) {
            val session = getSession(username, password)
            if (session != "") {
                call.respond(session)
            } else {
                notFound(call, "username or password not found")
            }
        } else {
            badRequest(call)
        }
    }
    get("/user/{session}") {
        val session = call.parameters["session"]
        if (session != null) {
            val newSession = getSession(session)
            call.respond(newSession)
        } else {
            badRequest(call)
        }
    }
}

fun Route.createUserRoute() {
    post("/user/{username}/{password}") {
        val username = call.parameters["username"]
        val password = call.parameters["password"]
        if (username != null && password != null) {
            val session = createSession(username, password)
            if (session != null) {
                call.respond(session)
            }
            badRequest(call, "username has been registered")
        }
        badRequest(call)
    }
}

fun Application.registerUserRoutes() {
    routing {
        getUserSessionRoute()
        createUserRoute()
    }
}