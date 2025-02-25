plugins {
    id("java")
    id("io.papermc.paperweight.userdev") version "2.0.0-beta.14"
    id ("com.github.johnrengelman.shadow") version "8.1.1"
}

group = "me.mogubea"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()

    maven {
        name = "papermc"
        url = uri("https://repo.papermc.io/repository/maven-public/")
    }

    maven {
        name = "protocol"
        url = uri("https://repo.dmulloy2.net/repository/public/" )
    }

}

dependencies {
    implementation("com.github.ben-manes.caffeine:caffeine:3.2.0")
    compileOnly("io.papermc.paper:paper-api:1.21.4-R0.1-SNAPSHOT")
    compileOnly("com.comphenix.protocol:ProtocolLib:5.1.0")
    paperweight.paperDevBundle("1.21.4-R0.1-SNAPSHOT")
}

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(21))
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
    paperweight.reobfArtifactConfiguration = io.papermc.paperweight.userdev.ReobfArtifactConfiguration.MOJANG_PRODUCTION
}

tasks.test {
    useJUnitPlatform()
}

tasks.shadowJar {
    destinationDirectory = file("C:\\Users\\Brandon\\Desktop\\Minecraft Servers\\Bens Bens\\plugins")
}