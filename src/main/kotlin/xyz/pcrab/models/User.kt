package xyz.pcrab.models

import io.ktor.auth.*
import kotlinx.serialization.Serializable
import xyz.pcrab.routes.isValidSerialNumber

@Serializable
class User(
    val username: String,
    val password: String,
    val serialNumber: String? = null
) {
    fun isValid(): Boolean {
        val username = this.username
        val password = this.password
        if (username.isNotBlank() && password.isNotBlank() ) {
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
        return DbUser.getDbUser(this)
    }

    fun dbCreate(): UserCheckStatus {
//        val dbUser = User(
//            username = this.username,
//            password = this.password.encryptThroughSHA3512(),
//            serialNumber = this.serialNumber
//        )
        return DbUser.createDbUser(this)
    }

}

data class UserSession(
    val username: String
) : Principal {
    fun isValid(): Boolean {
        if (this.username.isNotBlank()) {
            return true
        }
        return false
    }
}

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

//private fun String.encryptThroughSHA3512(): String {
//    var str = this
//    for (i in 1..100) {
//        str += i.toString()
//        str = MessageDigest.getInstance("SHA3-512").digest(this.toByteArray())
//            .joinToString(separator = "") { eachByte -> "%02x".format(eachByte) }
//    }
//    return str
//}
