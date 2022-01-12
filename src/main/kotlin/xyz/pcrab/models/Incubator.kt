package xyz.pcrab.models

import kotlinx.serialization.Serializable

@Serializable
data class IncubatorControl(
    val id: Int, // 3
    val temperature: Double, // 5 +xxx.x
    val co2: Double, // 5
    val dust: Double, // 4
    val light: Int, // 4
    val water: Int, // 1
)

@Serializable
data class IncubatorControlGroup(
    val serialNumber: String,
    val incubatorControls: MutableList<IncubatorControl>
)

@Serializable
data class Incubator(
    val id: Int, // 3
    val temperature: Double, // 5 xx.x
    val co2: Double, // 5
    val dust: Double, // 4
    val light: Int, // 4
    val water: Int, //1
    // all control takes 2
    val pi: Boolean,
    val fan1: Boolean,
    val fan2: Boolean,
    val pump: Boolean,
    val beep: Boolean,
    val led: Boolean,
)

@Serializable
data class IncubatorGroup(
    val serialNumber: String, val incubators: MutableList<Incubator>
) {
    constructor(content: String) : this(serialNumber = content.substring(0 until 19), incubators = mutableListOf()) {
        val incubatorControls = content.substring(19 until content.length)
        for (count in 0 until incubatorControls.length / 24) {
            this.incubators.add(
                buildIncubator(incubatorControls.substring(24 * count until 24 * (count + 1)))
            )
        }
    }
}

fun buildIncubatorControl(content: String): IncubatorControl {
    val id = content.substring(0..2).toInt()
    val temperature = content.substring(3..7).toDouble()
    val co2 = content.substring(8 ..12).toDouble()
    val dust = content.substring(13 ..16).toDouble()
    val light = content.substring(17 ..20).toInt()
    val water = content.substring(21).toInt()
    return IncubatorControl(id, temperature, co2, dust, light, water)
}

fun buildIncubator(content: String): Incubator {
    val incubatorControl = buildIncubatorControl(content.substring(0..21))
    val control = content.substring(22 ..23).toInt()
    val pi = 0b011111 or control == 0b111111
    val fan1 = 0b101111 or control == 0b111111
    val fan2 = 0b110111 or control == 0b111111
    val pump = 0b111011 or control == 0b111111
    val beep = 0b111101 or control == 0b111111
    val led = 0b111110 or control == 0b111111
    return Incubator(
        incubatorControl.id,
        incubatorControl.temperature,
        incubatorControl.co2,
        incubatorControl.dust,
        incubatorControl.light,
        incubatorControl.water,
        pi, fan1, fan2, pump, beep, led
    )
}

// Client side, need object
fun getIncubatorGroup(serialNumber: String): IncubatorGroup? {
    return getContent(serialNumber)
}

// ESP8266 side, need String
fun getIncubatorControlGroup(serialNumber: String): String {
    val group = getContentControl(serialNumber) ?: return ""
    var str = group.serialNumber
    for (incubatorControl in group.incubatorControls) {
        str += String.format(
            "%03d%03d%04d%02d%02d%1d",
            incubatorControl.id,
            incubatorControl.temperature,
            incubatorControl.co2,
            incubatorControl.dust,
            incubatorControl.light,
            incubatorControl.water,
        )
    }
    return str
}

// ESP8266 side, receive String
fun updateIncubatorGroup(content: IncubatorGroup) {
    updateContent(content)
}

// Client side, need object
fun updateIncubatorControlGroup(content: IncubatorControlGroup) {
    updateContentControl(content)
}
