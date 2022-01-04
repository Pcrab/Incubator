package xyz.pcrab.models

import kotlinx.serialization.Serializable

@Serializable
data class User (
    val username: String,
    val password: String,
    val serialNumber: String
)

@Serializable
data class UserSession (
    val username: String
)
fun getUser(username: String, password: String): User? {
    return getDbUser(username, password)
}

fun createUser(username: String, password: String, serialNumber: String): String? {
    return createDbUser(User(username, password, serialNumber))
}