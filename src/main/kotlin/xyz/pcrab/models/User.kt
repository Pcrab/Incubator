package xyz.pcrab.models

import kotlinx.serialization.Serializable
import java.security.MessageDigest

@Serializable
data class User(
    val username: String, val password: String, val serialNumber: String?
)

enum class UserCheckStatus {
    SUCCESS,
    USERNAME,
    SERIALNUMBER,
}

fun String.encryptThroughSHA3512(): String {
    var str = this
    for (i in 1..100) {
        str += i.toString()
        str = MessageDigest.getInstance("SHA3-512").digest(this.toByteArray())
            .joinToString(separator = "") { eachByte -> "%02x".format(eachByte) }
    }
    return str
}

fun isValidUsername(username: String): Boolean {
    for (c in username) {
        if (!c.isLetterOrDigit()) {
            return false
        }
    }
    return true
}

fun isValidPassword(password: String): Boolean {
    var hasDigit = false
    var hasLetter = false
    for (c in password) {
        if (c.isDigit()) {
            hasDigit = true
        } else if (c.isLetter()) {
            hasLetter = true
        }
    }
    return hasDigit && hasLetter
}

fun getUser(username: String, password: String): User? {
    return getDbUser(username, password.encryptThroughSHA3512())
}

fun createUser(username: String, password: String, serialNumber: String): UserCheckStatus {
    return createDbUser(User(username, password.encryptThroughSHA3512(), serialNumber))
}