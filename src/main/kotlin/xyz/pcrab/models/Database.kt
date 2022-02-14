package xyz.pcrab.models

import org.litote.kmongo.*

private const val uri = "mongodb://root:example@localhost:27017"
private const val dbName = "incubators"
private const val incubatorCollectionName = "incubator"
private const val userCollectionName = "users"
private val incubatorDb = KMongo.createClient(uri).getDatabase(dbName)
private val incubatorCol = incubatorDb.getCollection<IncubatorList>(incubatorCollectionName)
private val userCol = incubatorDb.getCollection<User>(userCollectionName)

/*
 * =============================================
 * ============   Incubator Part   =============
 * =============================================
 */

data class IncubatorList(
    private val serialNumber: String,
    private val incubators: MutableList<Incubator>,
    private val incubatorControls: MutableList<IncubatorControl>
) {
    companion object {
        fun updateContent(content: IncubatorGroup) {
            incubatorCol.updateOne(
                IncubatorList::serialNumber eq content.serialNumber,
                setValue(IncubatorList::incubators, content.incubators)
            )
        }

        fun updateContentControl(content: IncubatorControlGroup) {
            incubatorCol.updateOne(
                IncubatorList::serialNumber eq content.serialNumber,
                setValue(IncubatorList::incubatorControls, content.incubatorControls)
            )
        }

        fun getContent(serialNumber: String): IncubatorGroup? {
            val incubatorList = incubatorCol.findOne(IncubatorList::serialNumber eq serialNumber)?.incubators ?: return null
            return IncubatorGroup(
                serialNumber = serialNumber,
                incubators = incubatorList
            )
        }

        fun getContentControl(serialNumber: String): IncubatorControlGroup? {
            val incubatorControlList =
                incubatorCol.findOne(IncubatorList::serialNumber eq serialNumber)?.incubatorControls ?: return null
            return IncubatorControlGroup(
                serialNumber = serialNumber,
                incubatorControls = incubatorControlList
            )
        }

    }
}

/*
 * =============================================
 * ===============   User Part   ===============
 * =============================================
 */

object DbUser {
    fun getDbUser(user: User): Boolean {
        return userCol.findOne(User::username eq user.username, User::password eq user.password) != null
    }

    fun getDbUserUnsafe(username: String): User? {
        return userCol.findOne(User::username eq username)
    }

    fun createDbUser(user: User): UserCheckStatus {
        if (getDbUserUnsafe(user.username) != null) {
            return UserCheckStatus.USERNAME
        }
        val userWithSerialNumber = userCol.find(User::serialNumber eq user.serialNumber)
        if (!(0..10).contains(userWithSerialNumber.count())) {
            return UserCheckStatus.SERIALNUMBER
        }
        userCol.insertOne(
            User(
                user.username,
                user.password,
                user.serialNumber
            )
        )
        return UserCheckStatus.SUCCESS
    }
}
