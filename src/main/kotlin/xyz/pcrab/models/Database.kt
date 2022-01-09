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

fun getContents(serialNumber: String): IncubatorGroup {
    val col = incubatorDb.getCollection<IncubatorList>(serialNumber)
    val incubatorList = col.find().sort("{'_id':-1}").limit(5).toList()
    println(incubatorList)
    return IncubatorGroup(
        serialNumber = serialNumber,
        incubators = incubatorList[0].incubators
    )
}

fun getDbUser(username: String, password: String): User? {
    val col = incubatorDb.getCollection<User>(userCollection)
    return col.findOne(User::username eq username, User::password eq password)
}

fun getDbUserUnsafe(username: String): User? {
    val col = incubatorDb.getCollection<User>(userCollection)
    return col.findOne(User::username eq username)
}

fun createDbUser(user: User): UserCheckStatus {
    val col = incubatorDb.getCollection<User>(userCollection)
    if (getDbUserUnsafe(user.username) != null) {
        return UserCheckStatus.USERNAME
    }
    val userWithSerialNumber = col.find(User::serialNumber eq user.serialNumber)
    if (!(0..10).contains(userWithSerialNumber.count())) {
        return UserCheckStatus.SERIALNUMBER
    }
    col.insertOne(
        User(
            user.username,
            user.password,
            user.serialNumber
        )
    )
    return UserCheckStatus.SUCCESS
}