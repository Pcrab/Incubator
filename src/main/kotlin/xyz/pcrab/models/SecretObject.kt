package xyz.pcrab.models

import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import java.io.File

@Serializable
data class AuthUser(
    val username: String,
    val password: String,
)

@Serializable
data class SecretObject(
    val privateKeyString: String,
    val issuer: String,
    val audience: String,
    val realm: String
)

const val expireMaxTime = 3600000
const val refreshMinTime = 1800000

fun getSecretObject(args: Array<String>): SecretObject {
    var secretObject = SecretObject("", "", "", "")
    if (args.isNotEmpty() && File(args[0]).exists()) {
        secretObject = Json.decodeFromString(File(args[0]).readText())
    } else {
        val secretFile = {}::class.java.getResourceAsStream("/secret.json")
        if (secretFile != null) {
            secretObject =
                Json.decodeFromString(secretFile.bufferedReader().readText())
            println("Using default secret Json, which is not safe. Please DO NOT use it on production environment.")
        } else {
            println("!!! secret.json cannot be found in resources folder !!!")
        }
    }
    return secretObject
}

