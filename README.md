# Switchback

An offline-first hiking app for Android, written in Kotlin. Personal project.

Plan, record and follow hikes on topo-style maps that work with no signal. Maps are built from
OpenStreetMap and stored on the phone as PMTiles files.

## Status

Phase 0 (foundations): the app shows an offline map in its own style from a region file you
build and import. Plan, Record and Library tabs are placeholders for phase 1.

## Layout

| Path | What |
| --- | --- |
| `app/` | Android app: Compose UI, navigation, screens |
| `map/` | MapLibre wrapper, map style, offline region storage |
| `tools/tiles/` | Planetiler profile that builds a region's `.pmtiles` file (plain JVM, no Android SDK) |

## Build

- Android app: open in Android Studio, or `./gradlew assembleDebug` (needs the Android SDK, JDK 17+).
- Tests: `./gradlew testDebugUnitTest` and `./gradlew -p tools/tiles test` (JDK 21).
- A map to look at: see [tools/tiles/README.md](tools/tiles/README.md).

CI builds a debug APK on every push and pull request; download it from the run's artifacts.

## Stack

Kotlin, Jetpack Compose with Material 3, MapLibre Native Android, PMTiles, Planetiler.
Later phases add Room, a foreground location service, BRouter for offline routing, and a Wear OS
companion for Galaxy Watch.

## Credits

- Map data © [OpenStreetMap](https://www.openstreetmap.org/copyright) contributors (ODbL).
- Noto Sans glyphs (SIL Open Font License, `map/src/main/assets/fonts/OFL.txt`), via
  [protomaps/basemaps-assets](https://github.com/protomaps/basemaps-assets).
