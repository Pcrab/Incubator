package xyz.pcrab.routes

import io.ktor.application.*
import io.ktor.auth.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.sessions.*
import xyz.pcrab.models.*

fun Route.getIncubatorStatusRoute() {
    authenticate("inc-session") {
        get("/incubator") {
            val serialNumber = call.principal<IncubatorSession>()?.serialNumber ?: return@get notFound(call, "error serialNumber")
            val group = getIncubatorControlGroupString(serialNumber)
            call.respond(group)
        }
    }
}

fun Route.createNewIncubatorRoute() {
    authenticate("inc-session") {
        post("/incubator") {
            val content = call.receiveText()
            println(content)
            val serialNumber = call.principal<IncubatorSession>()?.serialNumber ?: return@post badRequest(call, "Wrong Session SerialNumber")
            updateIncubatorGroup(IncubatorGroup(serialNumber + content))
            call.respondText("finished!")
        }
        post("/incubator/control") {
            val content = call.receiveText()
            val serialNumber = call.principal<IncubatorSession>()?.serialNumber ?: return@post badRequest(call, "Wrong Session SerialNumber")
            println(content)
//            println(serialNumber + content)
//            println(call.request.header("Cookie"))
//            println(call.request.headers)
//            println("cookies: ${call.request.cookies.rawCookies}")
            updateIncubatorControlGroup(IncubatorControlGroup(serialNumber + content))
            call.respondText("finished!")
        }
    }
}

fun Route.loginRoute() {
    post("incubator/login") {
        val serialNumber = call.receiveText()
        if (getIncubatorGroup(serialNumber) != null) {
            println("$serialNumber logged in...")
            call.sessions.clear<IncubatorSession>()
            call.sessions.set(IncubatorSession(serialNumber = serialNumber))
            return@post call.respondText("Welcome, $serialNumber")
        }
    }
}

// Ensure SerialNUmber matches given Regex Pattern
const val defaultSerialNumber = """\d{4}-\d{4}-\d{4}-\d{4}"""
fun checkSerialNumber(serialNumber: String, pattern: Regex): Boolean {
    return pattern.matches(serialNumber)
}

fun isValidSerialNumber(serialNumber: String, pattern: String = defaultSerialNumber): Boolean {
    return checkSerialNumber(serialNumber, Regex(pattern))
}

fun Application.registerIncubatorRoutes() {
    routing {
        loginRoute()
        getIncubatorStatusRoute()
        createNewIncubatorRoute()
    }
}