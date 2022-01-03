package xyz.pcrab.models

import org.litote.kmongo.*
import java.time.LocalDateTime


private const val uri = "mongodb://root:example@localhost:27017"
private const val incubatorDbName = "incubators"
private const val userCollection = "users"
private val incubatorDb = KMongo.createClient(uri).getDatabase(incubatorDbName)

private class IncubatorList(
    val time: LocalDateTime,
    val incubators: MutableList<Incubator>
)

private class DbUser(
    val username: String,
    val password: String,
    val session: String,
    val serialNumber: String
)

fun updateContent(content: IncubatorGroup) {
    val incubators = mutableListOf<Incubator>()
    for (incubator in content.incubators) {
        incubators.add(incubator)
    }
    incubatorDb.getCollection<IncubatorList>(content.serialNumber).insertOne(
        IncubatorList(
            LocalDateTime.now(),
            incubators
        )
    )
}

fun getContent(serialNumber: String): IncubatorGroup? {
    val col = incubatorDb.getCollection<IncubatorList>(serialNumber)
//    col.ensureIndex("{'_id':-1}")
    val incubatorList = col.find().sort("{'_id':-1}").first()
    println(incubatorList)
    if (incubatorList != null) {
        return IncubatorGroup(
            serialNumber = serialNumber,
            incubators = incubatorList.incubators
        )
    }
    return null
}

fun getUserSession(user: User): String {
    val col = incubatorDb.getCollection<DbUser>(userCollection)
    val sessionUser = col.findOne(DbUser::username eq user.username, DbUser::password eq user.password)
    if (sessionUser != null) {
        return sessionUser.session
    }
    return ""
}

fun getUserSession(session: String): String {
    val col = incubatorDb.getCollection<DbUser>(userCollection)
    val sessionUser = col.findOne(DbUser::session eq session)
    if (sessionUser != null) {
        return sessionUser.session
    }
    return ""
}

fun createUserSession(user: User): String? {
    val session = user.username + user.password
    val col = incubatorDb.getCollection<DbUser>(userCollection)
    val dbuserThroughUsername = col.findOne(DbUser::username eq user.username)
    if (dbuserThroughUsername != null) {
        return null
    }
    val userWithSerialNumber = col.find(DbUser::serialNumber eq user.serialNumber)
    if (!(0..10).contains(userWithSerialNumber.count())) {
        return null
    }
    col.insertOne(
        DbUser(
            user.username,
            user.password,
            session,
            user.serialNumber
        )
    )
    return session
}