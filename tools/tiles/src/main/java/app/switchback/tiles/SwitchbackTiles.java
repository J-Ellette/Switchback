package app.switchback.tiles;

import com.onthegomap.planetiler.Planetiler;
import com.onthegomap.planetiler.config.Arguments;
import java.nio.file.Path;

/**
 * Builds one region's map tiles as a single PMTiles file.
 *
 * <pre>
 *   ./gradlew -p tools/tiles run --args="--area=oklahoma --download"
 * </pre>
 *
 * {@code --area} is a Geofabrik extract name (for example oklahoma, utah, colorado). The OSM
 * extract is cached in tools/tiles/data/sources and the result is written to
 * tools/tiles/data/&lt;area&gt;.pmtiles.
 */
public class SwitchbackTiles {

  public static void main(String[] args) throws Exception {
    Arguments arguments = Arguments.fromArgsOrConfigFile(args);
    String area = arguments.getString("area", "Geofabrik extract to build", "oklahoma");
    Path data = Path.of("data");

    Planetiler.create(arguments)
      .setProfile(new SwitchbackProfile())
      .addOsmSource("osm", data.resolve("sources").resolve(area + ".osm.pbf"), "geofabrik:" + area)
      .overwriteOutput(data.resolve(area + ".pmtiles"))
      .run();
  }
}
