package app.switchback.tiles;

import com.onthegomap.planetiler.FeatureCollector;
import com.onthegomap.planetiler.Profile;
import com.onthegomap.planetiler.reader.SourceFeature;
import com.onthegomap.planetiler.reader.osm.OsmElement;
import com.onthegomap.planetiler.reader.osm.OsmReader;
import com.onthegomap.planetiler.reader.osm.OsmRelationInfo;
import java.util.List;
import java.util.Map;

/**
 * Vector tile schema for Switchback: a hiking-first map built from OpenStreetMap.
 *
 * <p>Layers (all keep a {@code class} attribute that the app style switches on):
 * water, waterway, landcover, park, roads, paths, routes, pois, places. The schema is
 * documented in tools/tiles/README.md; keep the two in sync with the app style in
 * map/src/main/assets/style/switchback.json.
 */
public class SwitchbackProfile implements Profile {

  /** A waymarked hiking route relation, attached to each of its member ways. */
  public record HikingRoute(long id, String name, String network, String ref) implements OsmRelationInfo {}

  private static final Map<String, String> LANDCOVER = Map.ofEntries(
    Map.entry("wood", "wood"),
    Map.entry("forest", "wood"),
    Map.entry("scrub", "scrub"),
    Map.entry("heath", "scrub"),
    Map.entry("grassland", "grass"),
    Map.entry("meadow", "grass"),
    Map.entry("bare_rock", "rock"),
    Map.entry("scree", "rock"),
    Map.entry("sand", "sand"),
    Map.entry("wetland", "wetland")
  );

  @Override
  public List<OsmRelationInfo> preprocessOsmRelation(OsmElement.Relation relation) {
    if (relation.hasTag("type", "route") && relation.hasTag("route", "hiking", "foot")) {
      return List.of(new HikingRoute(
        relation.id(),
        relation.getString("name"),
        relation.getString("network"),
        relation.getString("ref")
      ));
    }
    return null;
  }

  @Override
  public void processFeature(SourceFeature sf, FeatureCollector features) {
    if (sf.canBePolygon()) {
      processArea(sf, features);
    }
    if (sf.canBeLine()) {
      processLine(sf, features);
    }
    processPoi(sf, features);
    if (sf.isPoint()) {
      processPlace(sf, features);
    }
  }

  private void processArea(SourceFeature sf, FeatureCollector features) {
    if (sf.hasTag("natural", "water") || sf.hasTag("waterway", "riverbank") || sf.hasTag("landuse", "reservoir", "basin")) {
      features.polygon("water")
        .setAttr("class", "lake")
        .setMinZoom(6)
        .setMinPixelSize(2);
      return;
    }

    String cover = LANDCOVER.get(sf.getString("natural"));
    if (cover == null) {
      cover = LANDCOVER.get(sf.getString("landuse"));
    }
    if (cover == null && sf.hasTag("wetland")) {
      cover = "wetland";
    }
    if (cover != null) {
      features.polygon("landcover")
        .setAttr("class", cover)
        .setMinZoom(8)
        .setMinPixelSize(2);
    }

    String parkClass = null;
    if (sf.hasTag("boundary", "national_park")) {
      parkClass = "national_park";
    } else if (sf.hasTag("boundary", "protected_area")) {
      parkClass = "protected_area";
    } else if (sf.hasTag("leisure", "nature_reserve")) {
      parkClass = "nature_reserve";
    } else if (sf.hasTag("leisure", "park")) {
      parkClass = "park";
    }
    if (parkClass != null) {
      features.polygon("park")
        .setAttr("class", parkClass)
        .setAttr("name", sf.getString("name"))
        .setMinZoom(parkClass.equals("park") ? 11 : 5)
        .setMinPixelSize(4);
    }
  }

  private void processLine(SourceFeature sf, FeatureCollector features) {
    String waterway = sf.getString("waterway");
    if ("river".equals(waterway) || "canal".equals(waterway)) {
      features.line("waterway").setAttr("class", "river").setAttr("name", sf.getString("name")).setMinZoom(8);
    } else if ("stream".equals(waterway) || "ditch".equals(waterway)) {
      features.line("waterway").setAttr("class", "stream").setAttr("name", sf.getString("name")).setMinZoom(12);
    }

    String highway = sf.getString("highway");
    if (highway != null) {
      String roadClass = switch (highway) {
        case "motorway", "trunk", "primary", "motorway_link", "trunk_link", "primary_link" -> "major";
        case "secondary", "tertiary", "secondary_link", "tertiary_link" -> "medium";
        case "residential", "unclassified", "service", "living_street", "road" -> "minor";
        case "track" -> "track";
        default -> null;
      };
      if (roadClass != null) {
        int minZoom = switch (roadClass) {
          case "major" -> 6;
          case "medium" -> 9;
          default -> 12;
        };
        features.line("roads")
          .setAttr("class", roadClass)
          .setAttr("name", sf.getString("name"))
          .setAttr("ref", sf.getString("ref"))
          .setAttr("tracktype", sf.getString("tracktype"))
          .setMinZoom(minZoom);
      }

      String pathClass = switch (highway) {
        case "path", "footway", "bridleway", "cycleway" -> "path";
        case "steps" -> "steps";
        default -> null;
      };
      if (pathClass != null) {
        features.line("paths")
          .setAttr("class", pathClass)
          .setAttr("name", sf.getString("name"))
          .setAttr("sac_scale", sf.getString("sac_scale"))
          .setAttr("trail_visibility", sf.getString("trail_visibility"))
          .setAttr("surface", sf.getString("surface"))
          .setMinZoom(12);
      }
    }

    for (OsmReader.RelationMember<HikingRoute> member : sf.relationInfo(HikingRoute.class)) {
      HikingRoute route = member.relation();
      features.line("routes")
        .setAttr("name", route.name())
        .setAttr("network", route.network())
        .setAttr("ref", route.ref())
        .setAttr("route_id", route.id())
        .setMinZoom(8);
    }
  }

  private void processPoi(SourceFeature sf, FeatureCollector features) {
    String poiClass = poiClass(sf);
    if (poiClass == null) {
      return;
    }
    if (!sf.isPoint() && !sf.canBePolygon()) {
      return;
    }
    var feature = sf.isPoint() ? features.point("pois") : features.pointOnSurface("pois");
    feature
      .setAttr("class", poiClass)
      .setAttr("name", sf.getString("name"))
      .setAttr("ele", sf.getString("ele"))
      .setMinZoom(poiMinZoom(poiClass));
  }

  static String poiClass(SourceFeature sf) {
    if (sf.hasTag("natural", "peak", "volcano")) return "peak";
    if (sf.hasTag("natural", "saddle")) return "saddle";
    if (sf.hasTag("natural", "spring")) return "spring";
    if (sf.hasTag("waterway", "waterfall")) return "waterfall";
    if (sf.hasTag("amenity", "drinking_water") || sf.hasTag("drinking_water", "yes") && sf.hasTag("amenity", "water_point")) return "water";
    if (sf.hasTag("tourism", "alpine_hut", "wilderness_hut")) return "hut";
    if (sf.hasTag("amenity", "shelter")) return "shelter";
    if (sf.hasTag("tourism", "camp_site") || sf.hasTag("tourism", "camp_pitch")) return "campsite";
    if (sf.hasTag("tourism", "viewpoint")) return "viewpoint";
    if (sf.hasTag("highway", "trailhead")) return "trailhead";
    if (sf.hasTag("tourism", "information") && sf.hasTag("information", "visitor_centre", "office")) return "visitor_center";
    if (sf.hasTag("amenity", "parking")) return "parking";
    if (sf.hasTag("amenity", "toilets")) return "toilets";
    return null;
  }

  private static int poiMinZoom(String poiClass) {
    return switch (poiClass) {
      case "peak" -> 10;
      case "trailhead", "visitor_center", "hut", "campsite", "waterfall" -> 12;
      case "parking", "toilets" -> 14;
      default -> 13;
    };
  }

  private void processPlace(SourceFeature sf, FeatureCollector features) {
    String place = sf.getString("place");
    if (place == null || sf.getString("name") == null) {
      return;
    }
    int minZoom = switch (place) {
      case "city" -> 5;
      case "town" -> 8;
      case "village" -> 11;
      case "hamlet", "locality" -> 13;
      default -> -1;
    };
    if (minZoom < 0) {
      return;
    }
    features.point("places")
      .setAttr("class", place)
      .setAttr("name", sf.getString("name"))
      .setMinZoom(minZoom);
  }

  @Override
  public String name() {
    return "Switchback";
  }

  @Override
  public String description() {
    return "Hiking-first vector tiles for the Switchback Android app";
  }

  @Override
  public String attribution() {
    return "<a href=\"https://www.openstreetmap.org/copyright\" target=\"_blank\">&copy; OpenStreetMap contributors</a>";
  }

  @Override
  public String version() {
    return "1";
  }
}
