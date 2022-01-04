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

fun getDbUser(username: String, password: String): User?{
    val col = incubatorDb.getCollection<User>(userCollection)
    return col.findOne(User::username eq username, User::password eq password)
}

fun createDbUser(user: User): String? {
    val session = user.username + user.password
    val col = incubatorDb.getCollection<User>(userCollection)
    val userThroughUsername = col.findOne(User::username eq user.username)
    if (userThroughUsername != null) {
        return null
    }
    val userWithSerialNumber = col.find(User::serialNumber eq user.serialNumber)
    if (!(0..10).contains(userWithSerialNumber.count())) {
        return null
    }
    col.insertOne(
        User(
            user.username,
            user.password,
            user.serialNumber
        )
    )
    return session
}