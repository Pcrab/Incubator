package xyz.pcrab.models

import kotlinx.serialization.Serializable

@Serializable
data class User (
    val username: String,
    val password: String,
)

fun getSession(username: String, password: String): String {
    return getUserSession(User(username, password))
}

fun getSession(session: String): String {
    return getUserSession(session)
}

fun createSession(username: String, password: String): String? {
    return createUserSession(User(username, password))
}