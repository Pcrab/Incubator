package xyz.pcrab.routes

import io.ktor.application.*
import io.ktor.http.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import xyz.pcrab.models.*

fun Route.getIncubatorStatusRoute() {
    get("/incubator/{serialNumber}/{id?}") {
        val group = getIncubatorGroup(getIncubatorSerialNumber(call)) ?: return@get notFound(call)
        val id = getIncubatorId(call)
        if (id != null) {
            val incubator = getIncubator(group, id) ?: return@get notFound(call)
            call.respond(incubator)
        } else {
            call.respond(group)
        }
    }
}

fun Route.createNewIncubatorRoute() {
    post("/incubator/{serialNumber}") {
        val serialNumber = getIncubatorSerialNumber(call)
        val incubatorGroup = call.receive<IncubatorGroup>()
        updateIncubatorGroup(serialNumber, incubatorGroup)
        call.respondText("finished!", status = HttpStatusCode.OK)
    }
}

// Ensure SerialNUmber matches given Regex Pattern
const val defaultSerialNumber = """\d{4}-\d{4}-\d{4}-\d{4}"""
fun checkSerialNumber(serialNumber: String, pattern: Regex): Boolean {
    return pattern.matches(serialNumber)
}

fun checkSerialNumber(serialNumber: String, pattern: String = defaultSerialNumber): Boolean {
    return checkSerialNumber(serialNumber, Regex(pattern))
}

suspend fun getIncubatorSerialNumber(call: ApplicationCall, pattern: String = defaultSerialNumber): String {
    val serialNumber = call.parameters["serialNumber"]!!
    if (checkSerialNumber(serialNumber, pattern)) {
        return serialNumber
    }
    badRequest(call)
    return ""
}

suspend fun getIncubatorId(call: ApplicationCall): Int? {
    val id = call.parameters["id"]
    if (id != null) {
        try {
            return id.toInt()
        } catch (e: NumberFormatException) {
            badRequest(call)
        }
    }
    return null
}

fun Application.registerIncubatorRoutes() {
    routing {
        getIncubatorStatusRoute()
        createNewIncubatorRoute()
    }
}