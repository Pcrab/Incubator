package xyz.pcrab.routes

import io.ktor.application.*
import io.ktor.response.*
import io.ktor.routing.*
import xyz.pcrab.models.*

fun Route.getUserSessionRoute() {
    get("/user/{username}/{password}") {
        val username = call.parameters["username"]
        val password = call.parameters["password"]
        if (!username.isNullOrBlank() && !password.isNullOrBlank()) {
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
        if (!session.isNullOrBlank()) {
            val newSession = getSession(session)
            call.respond(newSession)
        } else {
            badRequest(call)
        }
    }
}

fun Route.createUserRoute() {
    post("/user/{username}/{password}/{serialNumber}") {
        val username = call.parameters["username"]
        val password = call.parameters["password"]
        val serialNumber = call.parameters["serialNumber"]
        if (!username.isNullOrBlank() && !password.isNullOrBlank() && !serialNumber.isNullOrBlank()) {
            val session = createSession(username, password, serialNumber)
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