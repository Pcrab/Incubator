package xyz.pcrab.models

import io.ktor.auth.*
import kotlinx.serialization.Serializable

private const val serialNumberLength = 19

@Serializable
data class IncubatorControl(
    val id: Int, // 1
    val temperatureLow: Double, // 5 0xx.x
    val temperatureHigh: Double, // 5 0xx.x
    val light: Int, // 1
    val dust: Int, // 1
)

const val incubatorControlLength = 13

@Serializable
data class IncubatorControlGroup(
    val serialNumber: String,
    val incubatorControls: MutableList<IncubatorControl>
) {
    constructor(content: String) : this(
        serialNumber = content.substring(0 until serialNumberLength),
        incubatorControls = mutableListOf()
    ) {
        val incubatorControls = content.substring(serialNumberLength until content.length)
        for (count in 0 until incubatorControls.length / incubatorControlLength) {
            this.incubatorControls.add(
                buildIncubatorControl(incubatorControls.substring(incubatorControlLength * count until incubatorControlLength * (count + 1)))
            )
        }
    }
}

@Serializable
data class Incubator(
    val id: Int, // 1
    val mode: Int, // 1
    val temperature: Double, // 5 xx.x
    val co2: Int, // 5
    val light: Int, // 5
    val dust: Int, // 5
    val water: Int, // 2
    val pi: Boolean,
    val fan1: Boolean,
    val fan2: Boolean,
    val pump: Boolean,
    val beep: Boolean,
    val led: Boolean,
    val time: Int, // 8
    val days: Int, // 1
)

private const val incubatorLength = 38

@Serializable
data class IncubatorGroup(
    val serialNumber: String, val incubators: MutableList<Incubator>
) {
    constructor(content: String) : this(
        serialNumber = content.substring(0 until serialNumberLength),
        incubators = mutableListOf()
    ) {
        val incubatorControls = content.substring(serialNumberLength until content.length)
        for (count in 0 until incubatorControls.length / incubatorLength) {
            this.incubators.add(
                buildIncubator(incubatorControls.substring(incubatorLength * count until incubatorLength * (count + 1)))
            )
        }
    }
}

private fun buildIncubatorControl(content: String): IncubatorControl {
    val id = content[0].toString().toInt()
    val temperatureHigh = content.substring(1..5).toDouble()
    val temperatureLow = content.substring(6..10).toDouble()
    val light = content[11].toString().toInt()
    val dust = content[12].toString().toInt()
    return IncubatorControl(id, temperatureLow, temperatureHigh, light, dust)
}

private fun buildIncubator(content: String): Incubator {
    val id = content[0].toString().toInt()
    val mode = content[1].toString().toInt()
    val temperature = content.substring(2..6).toDouble()
    val co2 = content.substring(7..11).toInt()
    val light = content.substring(12..16).toInt()
    val dust = content.substring(17..21).toInt()
    val water = content.substring(22..23).toInt()
    val pi = content[24] != '0'
    val fan1 = content[25] != '0'
    val fan2 = content[26] != '0'
    val pump = content[27] != '0'
    val beep = content[28] != '0'
    val led = content[29] != '0'
    val time = content.substring(30..37).toInt()
    val days = if (content[38] == '0') 7 else 10
    return Incubator(
        id,
        mode,
        temperature,
        co2,
        light,
        dust,
        water,
        pi, fan1, fan2, pump, beep, led,
        time,
        days
    )
}

// Client side, need object
fun getIncubatorGroup(serialNumber: String): IncubatorGroup? {
    return IncubatorList.getContent(serialNumber)
}

// ESP8266 side, need String
fun getIncubatorControlGroupString(serialNumber: String): String {
    println(serialNumber)
    val group = IncubatorList.getContentControl(serialNumber) ?: return ""
    var str = ""
    for (incubatorControl in group.incubatorControls) {
        str += String.format(
            "%01d%05.1f%05.1f%1d%1d",
            incubatorControl.id,
            incubatorControl.temperatureHigh,
            incubatorControl.temperatureLow,
            incubatorControl.light,
            incubatorControl.dust,
        )
    }
    println(str)
    return str
}

// Client side, need object
fun getIncubatorControlGroup(serialNumber: String): IncubatorControlGroup? {
    return IncubatorList.getContentControl(serialNumber)
}

// ESP8266 side, receive String
fun updateIncubatorGroup(content: IncubatorGroup) {
    IncubatorList.updateContent(content)
}

// Client side, need object
fun updateIncubatorControlGroup(content: IncubatorControlGroup) {
    IncubatorList.updateContentControl(content)
}
data class IncubatorSession(
    val serialNumber: String
) : Principal
