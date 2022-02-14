package xyz.pcrab.routes

import io.ktor.application.*
import io.ktor.auth.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.sessions.*
import xyz.pcrab.models.*
import xyz.pcrab.models.DbUser.getDbUserUnsafe

fun Route.authRoute() {
    post("/user/login") {
        try {
            val user = call.receive<User>()
            println("$user logged in.")

            if (user.isValid()) {
                if (user.checkDb()) {
                    call.sessions.set(UserSession(username = user.username))
                    return@post call.respondText("welcome, $user.username")
                } else {
                    return@post badRequest(call, "username already been used")
                }
            }
            return@post notFound(call, "username or password not correct")
        } catch (e: Exception) {
            return@post badRequest(call, "Wrong Format")
        }
    }

    authenticate("auth-session") {
        get("/user/hello") {
            val user = call.principal<UserSession>()
            if (user != null && user.isValid()) {
                call.sessions.set(UserSession(username = user.username))
                return@get call.respondText("Cookie refreshed!")
            } else {
                return@get badRequest(call, "wrong cookie")
            }
        }
        get("/user/logout") {
            call.sessions.clear<UserSession>()
            return@get call.respondText("Logout succeeded")
        }
        get("/user/incubator") {
            val serialNumber = getSerialNumber(call) ?: return@get badRequest(call, "Wrong cookie!")
            val incubatorGroup = getIncubatorGroup(serialNumber)
            if (incubatorGroup != null) {
                return@get call.respond(incubatorGroup)
            } else {
                return@get badRequest(call, "Wrong username or serialNumber")
            }
        }
        post("/user/incubatorControl") {
            val incubatorControlGroup = call.receive<IncubatorControlGroup>()
            updateIncubatorControlGroup(incubatorControlGroup)
            return@post call.respondText("finish!")
        }
        get("/user/incubatorControl") {
            val serialNumber = getSerialNumber(call) ?: return@get badRequest(call, "Wrong cookie!")
            val incubatorControlGroup = getIncubatorControlGroup(serialNumber)
            if (incubatorControlGroup != null) {
                return@get call.respond(incubatorControlGroup)
            } else {
                return@get badRequest(call, "Wrong username or serialNumber")
            }
        }
    }
}

private fun getUsername(call: ApplicationCall): String? {
    val user = call.principal<UserSession>() ?: return null
    if (user.isValid()) {
        return user.username
    }
    return null
}

private fun getSerialNumber(call: ApplicationCall): String? {
    val username = getUsername(call) ?: return null
    val serialNumber = getDbUserUnsafe(username)?.serialNumber ?: return null
    if (!isValidSerialNumber(serialNumber)) {
        return null
    }
    return serialNumber
}

fun Route.createUserRoute() {
    post("/user/{username}/{password}/{serialNumber}") {
        val username = call.parameters["username"]
        val password = call.parameters["password"]
        val serialNumber = call.parameters["serialNumber"]
        if (username != null && password != null) {
            val user = User(
                username,
                password,
                serialNumber,
            )
            if (user.isValid()) {
                return@post when (user.dbCreate()) {
                    UserCheckStatus.USERNAME -> badRequest(call, "Username already Exists")
                    UserCheckStatus.SERIALNUMBER -> badRequest(call, "Too many registrations")
                    UserCheckStatus.SUCCESS -> call.respond("")
                }
            }
        }
        return@post badRequest(call, "Wrong Username or Password or SerialNumber")
    }
}

fun Application.registerUserRoutes() {
    routing {
        authRoute()
        createUserRoute()
    }
}