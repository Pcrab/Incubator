package xyz.pcrab

import com.auth0.jwk.JwkProviderBuilder
import io.ktor.application.*
import io.ktor.auth.*
import io.ktor.auth.jwt.*
import io.ktor.features.*
import io.ktor.http.*
import io.ktor.response.*
import io.ktor.serialization.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import xyz.pcrab.models.getSecretObject
import xyz.pcrab.plugins.*
import java.util.concurrent.TimeUnit


fun main(args: Array<String>) {
    println(args)

    val secretObject = getSecretObject(args)
    val jwkProvider = JwkProviderBuilder(secretObject.issuer)
        .cached(10, 24, TimeUnit.HOURS)
        .rateLimited(10, 1, TimeUnit.MINUTES)
        .build()
    embeddedServer(Netty, port = 8080) {
        install(ContentNegotiation) {
            json()
        }
        install(CORS) {
            anyHost()
            header(HttpHeaders.ContentType)
//            header(HttpHeaders.Authorization)
        }
        install(Authentication) {
            jwt("auth-jwt") {
                realm = secretObject.realm
                verifier(jwkProvider, secretObject.issuer) {
                    acceptLeeway(3)
                }
                validate { credential ->
                    if (credential.payload.getClaim("username").asString() != "") {
                        JWTPrincipal(credential.payload)
                    } else {
                        null
                    }
                }
                challenge { _, _ ->
                    call.respond("{\"token\": null}")
                }
            }
        }
        configureRouting(secretObject, jwkProvider)
        configureMonitoring()
    }.start(wait = true)
}


/**
 * ssh-keygen -t rsa -b 4096 -m PKCS8 -f jwtRS256.key
 * openssl rsa -in jwtRS256.key -pubout -out jwtRS256.key.pub
 * then use online converter
 * https://8gwifi.org/jwkconvertfunctions.jsp
 */