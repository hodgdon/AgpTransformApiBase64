@file:Suppress("UnstableApiUsage")
plugins {
    `kotlin-dsl`
}

repositories {
    google()
    jcenter()
}

tasks.withType<Test> {
    useJUnitPlatform()
    testLogging {
        showStandardStreams = true
    }
}

dependencies {
    implementation("com.android.tools.build:gradle:3.5.0-beta04")
    // kotlin gradle plugin 1.3.40 seems to think that kotlin's String or ByteArray are part of the kotlin stdlib
    // I don't think that they are.  Maybe a bug?
    implementation("org.jetbrains.kotlin:kotlin-gradle-plugin:1.3.31")
    implementation("org.ow2.asm:asm:7.1")
    implementation("org.ow2.asm:asm-util:7.1")
    implementation("com.squareup.okio:okio:2.2.2")

    testImplementation("org.junit.jupiter:junit-jupiter-api:5.4.2")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.4.2")
}