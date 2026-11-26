package app.switchback.tiles;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import com.onthegomap.planetiler.geo.GeoUtils;
import com.onthegomap.planetiler.reader.SimpleFeature;
import java.util.Map;
import org.junit.jupiter.api.Test;

class SwitchbackProfileTest {

  private static String classOf(Map<String, Object> tags) {
    return SwitchbackProfile.poiClass(SimpleFeature.create(GeoUtils.point(-98.5, 34.7), tags));
  }

  @Test
  void classifiesHikingPois() {
    assertEquals("peak", classOf(Map.of("natural", "peak", "name", "Mount Scott")));
    assertEquals("spring", classOf(Map.of("natural", "spring")));
    assertEquals("water", classOf(Map.of("amenity", "drinking_water")));
    assertEquals("trailhead", classOf(Map.of("highway", "trailhead")));
    assertEquals("campsite", classOf(Map.of("tourism", "camp_site")));
  }

  @Test
  void ignoresUnrelatedFeatures() {
    assertNull(classOf(Map.of("amenity", "bank")));
    assertNull(classOf(Map.of()));
  }
}
