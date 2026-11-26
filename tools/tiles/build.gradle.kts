plugins {
    java
    application
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

dependencies {
    implementation("com.onthegomap.planetiler:planetiler-core:0.10.2")
    testImplementation("org.junit.jupiter:junit-jupiter:5.11.4")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

application {
    mainClass = "app.switchback.tiles.SwitchbackTiles"
    applicationDefaultJvmArgs = listOf("-Xmx" + (findProperty("tilesXmx") ?: "4g"))
}

tasks.named<JavaExec>("run") {
    // Run from this folder so data/ lands in tools/tiles/data.
    workingDir = projectDir
}

tasks.test {
    useJUnitPlatform()
}
