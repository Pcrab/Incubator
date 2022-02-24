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
    val days: Int, // 1
)

const val incubatorControlLength = 14

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
    val light: Boolean, // 1
    val dust: Boolean, // 1
    val water: Int, // 2
    val pi: Boolean,
    val fan1: Boolean,
    val fan2: Boolean,
    val pump: Boolean,
    val beep: Boolean,
    val led: Boolean,
    val time: String, // 8
)

private const val incubatorLength = 30

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
    val days = content[13].toString().toInt()
    return IncubatorControl(id, temperatureLow, temperatureHigh, light, dust, days)
}

private fun buildIncubator(content: String): Incubator {
    val id = content[0].toString().toInt()
    val mode = content[1].toString().toInt()
    val temperature = content.substring(2..6).toDouble()
    val co2 = content.substring(7..11).toInt()
    val light = content[12] != '0'
    val dust = content[13] != '0'
    val water = content.substring(14..15).toInt()
    val pi = content[16] != '0'
    val fan1 = content[17] != '0'
    val fan2 = content[18] != '0'
    val pump = content[19] != '0'
    val beep = content[20] != '0'
    val led = content[21] != '0'
    val time = content.substring(22..23) + "天 " +
            content.substring(24 ..25) + "时 " +
            content.substring(26..27) + "分 " +
            content.substring(28..29) + "秒"
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
            "%01d%05.1f%05.1f%1d%1d%1d",
            incubatorControl.id,
            incubatorControl.temperatureHigh,
            incubatorControl.temperatureLow,
            incubatorControl.light,
            incubatorControl.dust,
            incubatorControl.days
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
