plugins {
    kotlin("jvm") version "2.0.0"
}

group = "com.github.numq"
version = "1.0.0"

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.9.0")
    implementation("com.microsoft.onnxruntime:onnxruntime:1.20.0")
    implementation("ai.djl.pytorch:pytorch-engine:0.32.0")
    implementation("ai.djl.pytorch:pytorch-jni:2.5.1-0.32.0")
    testImplementation(kotlin("test"))
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.9.0")
    testImplementation("ai.djl.pytorch:pytorch-native-cpu:2.5.1:win-x86_64")
}

tasks.test {
    useJUnitPlatform()
}