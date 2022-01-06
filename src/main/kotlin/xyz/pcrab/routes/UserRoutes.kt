package xyz.pcrab.routes

import com.auth0.jwk.JwkProvider
import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import io.ktor.application.*
import io.ktor.auth.*
import io.ktor.auth.jwt.*
import io.ktor.http.content.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import xyz.pcrab.models.*
import java.io.File
import java.security.KeyFactory
import java.security.interfaces.RSAPrivateKey
import java.security.interfaces.RSAPublicKey
import java.security.spec.PKCS8EncodedKeySpec
import java.util.*

fun Route.authRoute(secretObject: SecretObject, jwkProvider: JwkProvider) {
    fun genToken(username: String): String? {
        val publicKey = jwkProvider.get("77aa6010-5b6c-4c91-a75e-c09931d8e45b").publicKey
        println("publicKey: " + publicKey)
        val keySpecPKCS8 = PKCS8EncodedKeySpec(Base64.getDecoder().decode(secretObject.privateKeyString))
        println("keySpecPKCS8: " + keySpecPKCS8)
        val privateKey = KeyFactory.getInstance("RSA").generatePrivate(keySpecPKCS8)
        println("privateKey: " + privateKey)
        return JWT.create()
            .withAudience(secretObject.audience)
            .withIssuer(secretObject.issuer)
            .withClaim("username", username)
            .withExpiresAt(Date(System.currentTimeMillis() + 10000))
            .sign(Algorithm.RSA256(publicKey as RSAPublicKey, privateKey as RSAPrivateKey))

    }

    post("/user/login") {
        val username: String
        val password: String
        try {
            print("user: ")
            val text = call.receiveText()
            val user = Json.decodeFromString<AuthUser>(text)
            println(text)
            username = user.username
            password = user.password
        } catch (e: Exception) {
            return@post badRequest(call, "Wrong Format")
        }
        if (username.isNotBlank() && password.isNotBlank()) {
            val dbUser = getUser(username, password)
            if (dbUser != null) {
                val token = genToken(username)
                println("token: " + token)
                call.respond(hashMapOf("token" to token))
            } else {
                notFound(call, "username or password not found")
            }
        } else {
            badRequest(call)
        }
    }

    authenticate("auth-jwt") {
        get("/hello") {
            val principle = call.principal<JWTPrincipal>()
            val username = principle!!.payload.getClaim("username").asString()
            val expiresAt = principle.expiresAt?.time?.minus(System.currentTimeMillis())
            call.respondText("Hello, $username! Token is expired at $expiresAt ms.")
        }
    }
}

fun Route.createUserRoute() {
    post("/user/{username}/{password}/{serialNumber}") {
        val username = call.parameters["username"]
        val password = call.parameters["password"]
        val serialNumber = call.parameters["serialNumber"]
        if (!username.isNullOrBlank() && !password.isNullOrBlank() && !serialNumber.isNullOrBlank()) {
            val user = createUser(username, password, serialNumber)
            if (user != null) {
                call.respond(user)
            } else {
                badRequest(call, "username has been registered")
            }
        } else {
            badRequest(call)
        }
    }
}

fun Application.registerUserRoutes(secretObject: SecretObject, jwkProvider: JwkProvider) {
    routing {
        authRoute(secretObject, jwkProvider)
        createUserRoute()

        static(".well-known") {
            staticRootFolder = File("certs")
            file("jwks.json")
        }
    }
}