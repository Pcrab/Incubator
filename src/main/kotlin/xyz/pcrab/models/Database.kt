package xyz.pcrab.models

import org.litote.kmongo.*
import java.time.LocalDateTime


private const val uri = "mongodb://root:example@localhost:27017"
private const val dbName = "incubators"
private val db = KMongo.createClient(uri).getDatabase(dbName)
private class IncubatorList(
    val time: LocalDateTime,
    val incubators: MutableList<Incubator>
)

fun updateContent(content: IncubatorGroup) {
    val incubators = mutableListOf<Incubator>()
    for (incubator in content.incubators) {
        incubators.add(incubator)
    }
    db.getCollection<IncubatorList>(content.serialNumber).insertOne(IncubatorList(
        LocalDateTime.now(),
        incubators
    ))
}

fun getContent(serialNumber: String): IncubatorGroup? {
    val col = db.getCollection<IncubatorList>(serialNumber)
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
