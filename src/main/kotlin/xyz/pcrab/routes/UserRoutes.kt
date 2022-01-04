package xyz.pcrab.routes

import io.ktor.application.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.sessions.*
import xyz.pcrab.models.*

fun Route.getUserSessionRoute() {
    get("/user/{username}/{password}") {
        val username = call.parameters["username"]
        val password = call.parameters["password"]
        if (!username.isNullOrBlank() && !password.isNullOrBlank()) {
            val user = getUser(username, password)
            if (user != null) {
                call.sessions.set(UserSession(username = username))
                call.respond(user)
            } else {
                notFound(call, "username or password not found")
            }
        } else {
            badRequest(call)
        }
    }
}

fun Route.userGetIncubatorRoute() {
    get("/user/{serialNumber}") {
        val serialNumber = call.parameters["serialNumber"]
    }
}

fun Route.hello() {
    get("/hello") {
        val username = call.sessions.get<UserSession>()?.username
        if (username != null) {
            call.respond("Hello, $username")
        } else {
            notFound(call)
        }
    }
}

fun Route.createUserRoute() {
    post("/user/{username}/{password}/{serialNumber}") {
        val username = call.parameters["username"]
        val password = call.parameters["password"]
        val serialNumber = call.parameters["serialNumber"]
        if (!username.isNullOrBlank() && !password.isNullOrBlank() && !serialNumber.isNullOrBlank()) {
            val user = createUser(username, password, serialNumber)
            if (user != null) {
                call.respond(user)
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
        userGetIncubatorRoute()
        hello()
    }
}