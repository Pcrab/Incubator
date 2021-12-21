package xyz.pcrab.models

import kotlinx.serialization.Serializable

@Serializable
data class Incubator(
    val id: Int
)

@Serializable
data class IncubatorGroup(
    val serialNumber: String,
    val incubators: MutableList<Incubator>
)

fun updateIncubatorGroup(serialNumber: String, content: IncubatorGroup) {
    updateContent(content)
}

fun getIncubatorGroup(serialNumber: String): IncubatorGroup? {
    return getContent(serialNumber)
}

fun getIncubator(group: IncubatorGroup, id: Int): Incubator? {
    return group.incubators.find { it.id == id }
}

