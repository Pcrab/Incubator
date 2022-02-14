package xyz.pcrab.models

import org.litote.kmongo.*

private const val uri = "mongodb://root:example@localhost:27017"
private const val dbName = "incubators"
private const val incubatorCollectionName = "incubator"
private const val userCollectionName = "users"
private val incubatorDb = KMongo.createClient(uri).getDatabase(dbName)
private val incubatorCol = incubatorDb.getCollection<IncubatorList>(incubatorCollectionName)
private val userCol = incubatorDb.getCollection<DbUser>(userCollectionName)

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
            val incubatorList =
                incubatorCol.findOne(IncubatorList::serialNumber eq serialNumber)?.incubators ?: return null
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

data class DbUser(
    val username: String,
    val password: String,
    val salt: String,
    val serialNumber: String?,
) {
    companion object {

        fun getDbUser(user: User): Boolean {
            return userCol.findOne(DbUser::username eq user.username, DbUser::password eq user.password) != null
        }

        fun getDbUserUnsafe(username: String): DbUser? {
            return userCol.findOne(DbUser::username eq username)
        }

        fun getSalt(username: String): String? {
            return userCol.findOne(DbUser::username eq username)?.salt
        }

        fun createDbUser(user: DbUser): UserCheckStatus {
            if (getDbUserUnsafe(user.username) != null) {
                return UserCheckStatus.USERNAME
            }
            val userWithSerialNumber = userCol.find(DbUser::serialNumber eq user.serialNumber)
            if (!(0..10).contains(userWithSerialNumber.count())) {
                return UserCheckStatus.SERIALNUMBER
            }
            userCol.insertOne(
                DbUser(
                    user.username,
                    user.password,
                    user.salt,
                    user.serialNumber
                )
            )
            return UserCheckStatus.SUCCESS
        }
    }
}
