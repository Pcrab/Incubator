package xyz.pcrab.models

import kotlinx.serialization.Serializable
import java.security.MessageDigest

@Serializable
data class User(
    val username: String, val password: String, val serialNumber: String
)

@Serializable
data class UserSession(
    val username: String
)

fun String.encryptThroughSHA256(): String {
    return MessageDigest.getInstance("SHA-256").digest(this.toByteArray())
        .joinToString(separator = "") { eachByte -> "%02x".format(eachByte) }
}

fun getUser(username: String, password: String): User? {
    return getDbUser(username, password.encryptThroughSHA256())
}

fun createUser(username: String, password: String, serialNumber: String): String? {
    return createDbUser(User(username, password.encryptThroughSHA256(), serialNumber))
}