package xyz.pcrab.models

import io.ktor.auth.*
import kotlinx.serialization.Serializable
import xyz.pcrab.routes.isValidSerialNumber
import java.security.MessageDigest
import java.security.SecureRandom

@Serializable
class User(
    val username: String,
    val password: String,
    private val serialNumber: String? = null
) {
    fun isValid(): Boolean {
        val username = this.username
        val password = this.password
        if (username.isNotBlank() && password.isNotBlank()) {
            if (isValidUsername(username) && isValidPassword(password)) {
                return true
            }

        }
        return false
    }

    fun isValidWithSerialNumber(): Boolean {
        val username = this.username
        val password = this.password
        val serialNumber = this.serialNumber
        if (username.isNotBlank() && password.isNotBlank() && !serialNumber.isNullOrBlank()) {
            if (isValidUsername(username) && isValidPassword(password) && isValidSerialNumber(serialNumber)) {
                return true
            }

        }
        return false
    }

    fun dbCheck(): Boolean {
        val salt = DbUser.getSalt(this.username) ?: return false
        return DbUser.getDbUser(
            User(
                this.username,
                encryptPwd(this.password, salt),
            )
        )
    }

    fun dbCreate(): UserCheckStatus {
        val random = SecureRandom()
        var salt = ""
        for (i in 0..128) {
            salt += random.nextInt(16).toString(16)
        }
        println(salt)
        return DbUser.createDbUser(
            DbUser(
                this.username,
                encryptPwd(this.password, salt),
                salt,
                this.serialNumber
            )
        )
    }

}

data class UserSession(
    val username: String
) : Principal

enum class UserCheckStatus {
    SUCCESS,
    USERNAME,
    SERIALNUMBER,
}

private fun isValidUsername(username: String): Boolean {
    for (c in username) {
        if (!c.isLetterOrDigit()) {
            return false
        }
    }
    return true
}

private fun isValidPassword(password: String): Boolean {
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

private fun String.encryptThroughSHA3512(): String {
    return MessageDigest.getInstance("SHA3-512").digest(this.toByteArray())
        .joinToString(separator = "") { eachByte -> "%02x".format(eachByte) }
}

private fun encryptPwd(pwd: String, salt: String): String {
    var result = ""
    for (i in 0..10) {
        result += (pwd + salt).encryptThroughSHA3512()
    }
    return result
}