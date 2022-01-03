package xyz.pcrab.models

class User (
    val username: String,
    val password: String,
    val serialNumber: String
)

fun getSession(username: String, password: String): String {
    return getUserSession(User(username, password, ""))
}

fun getSession(session: String): String {
    return getUserSession(session)
}

fun createSession(username: String, password: String, serialNumber: String): String? {
    return createUserSession(User(username, password, serialNumber))
}