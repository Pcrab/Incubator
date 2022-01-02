package xyz.pcrab.routes

import io.ktor.application.*
import io.ktor.http.*
import io.ktor.response.*

suspend fun badRequest(call: ApplicationCall, text: String = "Bad Request") {
    call.respondText(
        text, status = HttpStatusCode.BadRequest
    )
}

//  suspend fun forbidden(call: ApplicationCall, text: String = "Forbidden") {
//      call.respondText(
//          text, status = HttpStatusCode.Forbidden
//      )
//  }

suspend fun notFound(call: ApplicationCall, text: String = "Not Found") {
    call.respondText(
        text, status = HttpStatusCode.NotFound
    )
}

