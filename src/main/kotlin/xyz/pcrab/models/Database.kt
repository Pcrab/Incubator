package xyz.pcrab.models

import org.litote.kmongo.*
import java.time.LocalDateTime


private const val uri = "mongodb://root:example@localhost:27017"
private const val incubatorDbName = "incubators"
private const val userCollection = "users"
private val incubatorDb = KMongo.createClient(uri).getDatabase(incubatorDbName)

/*
 * =============================================
 * ============   Incubator Part   =============
 * =============================================
 */

private class IncubatorList(
    val time: LocalDateTime,
    val incubators: MutableList<Incubator>
)

private class IncubatorControlList(
    @Suppress("unused")
    val time: LocalDateTime,
    val incubatorControls: MutableList<IncubatorControl>
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

fun updateContentControl(content: IncubatorControlGroup) {
    val incubatorControls = mutableListOf<IncubatorControl>()
    for (incubatorControl in content.incubatorControls) {
        incubatorControls.add(incubatorControl)
    }
    incubatorDb.getCollection<IncubatorControlList>("${content.serialNumber}-control").insertOne(
        IncubatorControlList(
            LocalDateTime.now(),
            incubatorControls
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

fun getContentControl(serialNumber: String): IncubatorControlGroup? {
    val col = incubatorDb.getCollection<IncubatorControlList>("$serialNumber-control")
    val incubatorControlList = col.find().sort("{'_id':-1}").first()
    println(incubatorControlList)
    if (incubatorControlList != null) {
        return IncubatorControlGroup(
            serialNumber = serialNumber,
            incubatorControls = incubatorControlList.incubatorControls
        )
    }
    return null
}

/*
 * =============================================
 * ===============   User Part   ===============
 * =============================================
 */

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