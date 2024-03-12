import org.jetbrains.kotlin.gradle.dsl.KotlinCompile

plugins {
    kotlin("jvm") version "1.9.22"
    kotlin("plugin.serialization") version "1.9.22"
}

group = "com.ivieleague"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation(kotlin("reflect"))
    api("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.3")
    api("org.jetbrains.kotlin:kotlin-compiler-embeddable:1.9.22")
//    api("org.jetbrains.kotlin:kotlin-script-util:1.9.22")
//    api("org.jetbrains.kotlin:kotlin-script-runtime:1.9.22")
//    api("org.jetbrains.kotlin:kotlin-scripting-compiler-embeddable:1.9.22")
    api("org.jetbrains.kotlin:kotlin-native-utils:1.9.22")

    api("org.eclipse.aether:aether-api:1.0.0.v20140518")
    api("org.eclipse.aether:aether-impl:1.0.0.v20140518")
    api("org.eclipse.aether:aether-util:1.0.0.v20140518")
    api("org.eclipse.aether:aether-connector-basic:1.0.0.v20140518")
    api("org.eclipse.aether:aether-transport-file:1.0.0.v20140518")
    api("org.eclipse.aether:aether-transport-http:1.0.0.v20140518")
    api("org.apache.maven:maven-aether-provider:3.1.0")
    api("org.redundent:kotlin-xml-builder:1.9.1")
    api("org.apache.commons:commons-text:1.11.0")
    api("org.jasypt:jasypt:1.9.3")

    api("org.junit.jupiter:junit-jupiter-api:5.8.1")
    api("org.junit.jupiter:junit-jupiter-engine:5.8.1")
    api("org.junit.platform:junit-platform-launcher:1.10.2")

    testImplementation("org.jetbrains.kotlin:kotlin-test")
}

tasks.withType(KotlinCompile::class) {

}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(17)
}