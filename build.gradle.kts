import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    kotlin("jvm") version "1.6.20"
    id("org.openjfx.javafxplugin") version "0.0.12"
    id("com.github.johnrengelman.shadow") version "7.1.2"
    application
}

group = "gh.marad"
version = "1.0.2"

application {
    mainClass.set("gh.marad.cmdwindow.MainKt")
}

repositories {
    jcenter()
    mavenCentral()
    maven { url = uri("https://repo.clojars.org") }
    google()
}

dependencies {
    implementation("org.graalvm.js:js:22.0.0.2")

    implementation("com.dustinredmond.fxtrayicon:FXTrayIcon:3.1.2")
    implementation("com.jfoenix:jfoenix:9.0.10")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.1")

    runtimeOnly("org.jetbrains.kotlin:kotlin-scripting-jsr223:1.5.21")
    testRuntimeOnly("org.jetbrains.kotlin:kotlin-scripting-jsr223:1.5.21")
    implementation("org.seleniumhq.selenium:selenium-java:3.141.59")

    implementation("io.arrow-kt:arrow-core:1.0.0")
    implementation("net.seidengarn:keepass-http-java-client:1.3")
    implementation("org.clojure:clojure:1.10.3")
    implementation("nrepl:nrepl:0.8.3")
    implementation("clj-http:clj-http:3.12.3")
    implementation("org.clojure:data.json:2.4.0")
    implementation("camel-snake-kebab:camel-snake-kebab:0.4.2")
    implementation("etaoin:etaoin:0.4.1")

    implementation(platform("org.http4k:http4k-bom:4.7.0.0"))
    implementation("org.http4k:http4k-core:4.14.0.0")
    implementation("org.http4k:http4k-server-netty:4.14.0.0")
    implementation("org.http4k:http4k-client-apache:4.14.0.0")

    implementation("net.java.dev.jna:jna:5.9.0")
    implementation("net.java.dev.jna:jna-platform:5.9.0")

    implementation("com.squareup.okhttp3:okhttp:4.9.2")
    implementation("com.fasterxml.jackson.core:jackson-databind:2.13.0")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.13.0")
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310:2.13.0")

    testImplementation("io.kotest:kotest-runner-junit5:4.6.3")
    testImplementation("io.kotest:kotest-assertions-core:4.6.3")
    testImplementation("io.kotest:kotest-property:4.6.3")
}

tasks.withType<KotlinCompile>() {
    kotlinOptions.jvmTarget = "14"
}

tasks.withType<Test> {
    useJUnitPlatform()
}

tasks.register<Copy>("copyUberJar") {
    dependsOn("shadowJar")
    from("$buildDir/libs/")
    include("*-all.jar")
    rename("cmd-window-$version-all\\.jar", "cmd-window.jar")
    into("$buildDir/dist")
}

tasks.register<Copy>("copyAhkDll") {
    from(relativePath("AutohotkeyV1.dll"))
    into("$buildDir/dist")
}

tasks.register("makeDist") {
    dependsOn("copyUberJar")
    dependsOn("copyAhkDll")
}

tasks.register<Copy>("installLocalDist") {
    dependsOn("makeDist")
    from("$buildDir/dist")
    into("C:\\apps\\cmd-window")
}

javafx {
    modules("javafx.controls")
}

tasks {
    "shadowJar"(ShadowJar::class) {
        isZip64 = true
    }
}