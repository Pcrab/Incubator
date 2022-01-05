package xyz.pcrab

import io.ktor.application.*
import io.ktor.auth.*
import io.ktor.auth.jwt.*
import io.ktor.features.*
import io.ktor.serialization.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.sessions.*
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import xyz.pcrab.models.UserSession
import xyz.pcrab.models.SecretObject
import xyz.pcrab.plugins.*
import java.io.File


fun main(args: Array<String>) {
    println(args)
    embeddedServer(Netty, port = 8080) {
        install(ContentNegotiation) {
            json()
        }
        install(CORS) {
            host("127.0.0.1")
            host("localhost:63342")
            allowCredentials = true
        }
        install(Sessions) {
            cookie<UserSession>("user_session", storage = SessionStorageMemory())
        }

        var secretPrivateKey = ""
        if (args.isNotEmpty() && File(args[0]).exists()) {
            secretPrivateKey = Json.decodeFromString<SecretObject>(File(args[0]).readText()).secretPrivateKey
        } else {
            val secretFile = {}::class.java.getResourceAsStream("/secret.json")
            if (secretFile != null) {
                secretPrivateKey = Json.decodeFromString<SecretObject>(secretFile.bufferedReader().readText()).secretPrivateKey
                println("Using default secret Json, which is not safe. Please DO NOT use it on production environment.")
            } else {
                println("!!! secret.json cannot be found in resources folder !!!")
            }
        }

        println(secretPrivateKey)

        install(Authentication) {
            jwt {

            }
        }
        configureRouting()
        configureMonitoring()
    }.start(wait = true)
}

