rootProject.name = "switchback-tiles"

dependencyResolutionManagement {
    repositories {
        mavenCentral()
        // Planetiler depends on GeoTools, which is published here.
        maven("https://repo.osgeo.org/repository/release/")
    }
}
