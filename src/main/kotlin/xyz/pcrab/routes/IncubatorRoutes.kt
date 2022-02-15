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
        get("/incubator/{serialNumber}") {
            val group = getIncubatorControlGroupString(getIncubatorSerialNumber(call))
            call.respond(group)
        }
    }
}

fun Route.createNewIncubatorRoute() {
    authenticate("inc-session") {
        post("/incubator") {
            val content = call.receiveText()
            updateIncubatorGroup(IncubatorGroup(content))
            call.respondText("finished!")
        }
        post("/incubator/control") {
            val content = call.receiveText()
            updateIncubatorControlGroup(IncubatorControlGroup(content))
            call.respondText("finished!")
        }
    }
}

fun Route.loginRoute() {
    post("incubator/login") {
        val serialNumber = call.receiveText()
        if (getIncubatorGroup(serialNumber) != null) {
            println("$serialNumber logged in...")
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

suspend fun getIncubatorSerialNumber(call: ApplicationCall, pattern: String = defaultSerialNumber): String {
    val serialNumber = call.parameters["serialNumber"]!!
    if (isValidSerialNumber(serialNumber, pattern)) {
        return serialNumber
    }
    badRequest(call)
    return ""
}

fun Application.registerIncubatorRoutes() {
    routing {
        loginRoute()
        getIncubatorStatusRoute()
        createNewIncubatorRoute()
    }
}