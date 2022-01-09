package xyz.pcrab.routes

import io.ktor.application.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import xyz.pcrab.models.*

fun Route.getIncubatorStatusRoute() {
    get("/incubator/{serialNumber}") {
        val group = getIncubatorControlGroup(getIncubatorSerialNumber(call))
        call.respond(group)
    }
}

fun Route.createNewIncubatorRoute() {
    post("/incubator") {
        val content = call.receiveText()
        updateIncubatorGroup(IncubatorGroup(content))
        call.respondText("finished!")
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
        getIncubatorStatusRoute()
        createNewIncubatorRoute()
    }
}