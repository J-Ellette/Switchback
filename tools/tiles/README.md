# Tile tool

Builds Switchback's offline map for one region as a single `.pmtiles` file, using
[Planetiler](https://github.com/onthegomap/planetiler) and OpenStreetMap data from Geofabrik.

## Build a region on your computer

Needs Java 21. From the repository root:

```sh
./gradlew -p tools/tiles run --args="--area=oklahoma --download"
```

`--area` is a Geofabrik extract name: a US state (`oklahoma`, `utah`, `colorado`, `arizona`) or
any other region Geofabrik lists. The OSM extract is cached in `tools/tiles/data/sources/`, and
the map is written to `tools/tiles/data/<area>.pmtiles`. Add `-PtilesXmx=8g` for large states.

## Build a region on GitHub

Actions tab, **Build map region**, **Run workflow**, enter the area. When it finishes, download
the `.pmtiles` file from the run's artifacts.

## Put it on the phone

Copy the file to the phone (USB, Google Drive, or `adb push oklahoma.pmtiles /sdcard/Download/`),
open Switchback, tap **Import a .pmtiles file**, and pick it. The map then works with no signal.

## Schema

Every layer has a `class` attribute the app style switches on. Keep this table, the profile
(`SwitchbackProfile.java`) and the app style (`map/src/main/assets/style/switchback.json`) in step.

| Layer | Geometry | Classes | Other attributes | From zoom |
| --- | --- | --- | --- | --- |
| water | polygon | lake | | 6 |
| waterway | line | river, stream | name | 8 / 12 |
| landcover | polygon | wood, scrub, grass, rock, sand, wetland | | 8 |
| park | polygon | national_park, protected_area, nature_reserve, park | name | 5 (parks 11) |
| roads | line | major, medium, minor, track | name, ref, tracktype | 6 / 9 / 12 |
| paths | line | path, steps | name, sac_scale, trail_visibility, surface | 12 |
| routes | line | (none) | name, network, ref, route_id | 8 |
| pois | point | peak, saddle, spring, waterfall, water, hut, shelter, campsite, viewpoint, trailhead, visitor_center, parking, toilets | name, ele | 10 to 14 |
| places | point | city, town, village, hamlet, locality | name | 5 to 13 |

Tiles go to zoom 14; the app overzooms past that.

## Data license

Map data © OpenStreetMap contributors, ODbL. The app shows this attribution on the map.
