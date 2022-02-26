val ktorVersion: String by project
val kotlinVersion: String by project
val logbackVersion: String by project

plugins {
    application
    kotlin("jvm") version "1.6.10"
    kotlin("plugin.serialization") version "1.6.10"
    // Package into fatJar(shadowJar)
    id("com.github.johnrengelman.shadow") version "7.0.0"
}

group = "xyz.pcrab"
version = "0.0.1"
application {
//    mainClass.set("io.ktor.server.netty.EngineMain")
    mainClass.set("xyz.pcrab.ApplicationKt")
}

repositories {
    mavenCentral()
}

tasks {
    shadowJar {
        manifest {
            attributes(Pair("Main-Class", "xyz.pcrab.ApplicationKt"))
        }
    }
}

dependencies {
    implementation("io.ktor:ktor-server-core:$ktorVersion")
    implementation("io.ktor:ktor-server-netty:$ktorVersion")
    implementation("ch.qos.logback:logback-classic:$logbackVersion")

    // Serialization
    implementation("io.ktor:ktor-serialization:$ktorVersion")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.3.2")

    implementation("io.ktor:ktor-server-sessions:$ktorVersion")
    implementation("io.ktor:ktor-auth:$ktorVersion")
    implementation("io.ktor:ktor-auth-jwt:$ktorVersion")

    testImplementation("io.ktor:ktor-server-tests:$ktorVersion")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit:$kotlinVersion")

    // KMongo
    implementation("org.litote.kmongo:kmongo:4.4.0")

    // Redis
    implementation("redis.clients:jedis:4.1.0")
}