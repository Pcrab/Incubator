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
        try {
            val user = call.receive<User>()
            println("$user logged in.")

            if (user.isValid()) {
                if (user.dbCheck()) {
                    call.sessions.set(UserSession(username = user.username))
                    return@post call.respondText("welcome, $user.username")
                }
            }
            return@post notFound(call, "username or password not correct")
        } catch (e: Exception) {
            return@post badRequest(call, "Wrong Format")
        }
    }

    authenticate("user-session") {
        get("/user/hello") {
            val user = call.principal<UserSession>()
            if (user != null) {
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
    return user.username
}

private fun getSerialNumber(call: ApplicationCall): String? {
    val username = getUsername(call) ?: return null
    val serialNumber = DbUser.getDbUserUnsafe(username)?.serialNumber ?: return null
    if (!isValidSerialNumber(serialNumber)) {
        return null
    }
    return serialNumber
}

fun Route.createUserRoute() {
    post("/user/create") {
        try {
            val user = call.receive<User>()

            if (user.isValidWithSerialNumber()) {
                return@post when (user.dbCreate()) {
                    UserCheckStatus.USERNAME -> badRequest(call, "Username already Exists")
                    UserCheckStatus.SERIALNUMBER -> badRequest(call, "Too many registrations")
                    UserCheckStatus.SUCCESS -> call.respond("")
                }
            }
            return@post notFound(call, "username or password or serialNumber not correct")
        } catch (e: Exception) {
            return@post badRequest(call, "Wrong Format")
        }
    }
}

fun Application.registerUserRoutes() {
    routing {
        authRoute()
        createUserRoute()
    }
}