package xyz.pcrab.routes

import io.ktor.application.*
import io.ktor.auth.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.sessions.*
import xyz.pcrab.models.*

fun Route.authRoute() {

    post("/user/login") {
        val username: String
        val password: String
        try {
            val user = call.receive<AuthUser>()
            println("$user")
            username = user.username
            password = user.password
        } catch (e: Exception) {
            return@post badRequest(call, "Wrong Format")
        }
        if (username.isNotBlank() && password.isNotBlank()) {
            val dbUser = getUser(username, password)
            if (dbUser != null) {
                call.sessions.set(UserSession(username = username))
                return@post call.respondText("welcome, $username")
            } else {
                return@post notFound(call, "username or password not found")
            }
        }
    }

    authenticate("auth-session") {
        get("/hello") {
            val principle = call.principal<UserSession>()!!
            val username = principle.username
            if (isValidUsername(username) && getDbUserUnsafe(username) != null) {
                call.sessions.set(UserSession(username = username))
                return@get call.respondText("Cookie refreshed!")
            } else {
                return@get badRequest(call, "Wrong cookie!")
            }
        }
        get("/user/logout") {
            call.sessions.clear<UserSession>()
            call.respondText("Logout succeeded")
        }
        get("/user/incubator") {
            val principle = call.principal<UserSession>()!!
            val username = principle.username
            call.sessions.set(UserSession(username = username))
            if (isValidUsername(username)) {
                val serialNumber = getDbUserUnsafe(username)?.serialNumber
                if (serialNumber != null && isValidSerialNumber(serialNumber)) {
                    val incubatorGroup = getIncubatorGroup(serialNumber)
                    if (incubatorGroup != null) {
                        return@get call.respond(incubatorGroup)
                    }
                }
                return@get notFound(call, "serialNumber associated to $username not found")
            }
            return@get badRequest(call, "Wrong username")
        }
    }
}

fun Route.createUserRoute() {
    post("/user/{username}/{password}/{serialNumber}") {
        val username = call.parameters["username"]
        val password = call.parameters["password"]
        val serialNumber = call.parameters["serialNumber"]
        if (!username.isNullOrBlank() && !password.isNullOrBlank() && !serialNumber.isNullOrBlank()) {
            if (isValidUsername(username) && isValidPassword(password) && isValidSerialNumber(serialNumber)) {
                return@post when (createUser(username, password, serialNumber)) {
                    UserCheckStatus.USERNAME -> badRequest(call, "Username already Exists")
                    UserCheckStatus.SERIALNUMBER -> badRequest(call, "Too many registrations")
                    UserCheckStatus.SUCCESS -> call.respond("")
                }
            } else {
                return@post badRequest(call, "Wrong Username or Password or SerialNumber")
            }
        }
    }
}

fun Application.registerUserRoutes() {
    routing {
        authRoute()
        createUserRoute()
    }
}