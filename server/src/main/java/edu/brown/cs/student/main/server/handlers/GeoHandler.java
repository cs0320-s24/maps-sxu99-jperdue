package edu.brown.cs.student.main.server.handlers;

import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.Moshi;
import com.squareup.moshi.Types;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;
import spark.Request;
import spark.Response;
import spark.Route;

public class GeoHandler implements Route {

  @Override
  public Object handle(Request request, Response response) {
    double minLat = Double.parseDouble(request.queryParams("minLat"));
    double maxLat = Double.parseDouble(request.queryParams("maxLat"));
    double minLong = Double.parseDouble(request.queryParams("minLong"));
    double maxLong = Double.parseDouble(request.queryParams("maxLong"));

    // Load GeoJSON data
    List<Region> regions = loadGeoJsonData();

    // Filter regions
    List<Region> filteredRegions =
        regions.stream()
            .filter(region -> isContainedIn(region, minLat, maxLat, minLong, maxLong))
            .collect(Collectors.toList());

    // Convert back to GeoJSON
    String geoJson = convertToGeoJson(filteredRegions);

    // Return GeoJSON data
    return geoJson;
  }

  private List<Region> loadGeoJsonData() {
    try {
      // Load the GeoJSON data from a file
      String geoJson =
          new String(
              Files.readAllBytes(
                  Paths.get(
                      "server/src/main/java/edu/brown/cs/student/main/server/data/fullDownload.json")));

      // Parse it into a list of Region objects using Moshi
      Moshi moshi = new Moshi.Builder().build();
      JsonAdapter<List<Region>> jsonAdapter =
          moshi.adapter(Types.newParameterizedType(List.class, Region.class));
      return jsonAdapter.fromJson(geoJson);
    } catch (IOException e) {
      e.printStackTrace();
      return null;
    }
  }

  private boolean isContainedIn(
      Region region, double minLat, double maxLat, double minLong, double maxLong) {
    // Get the boundary of the region
    Boundary boundary = region.getBoundary();

    // Check if the region's boundaries are within the specified boundaries
    return boundary.getMinLat() >= minLat
        && boundary.getMaxLat() <= maxLat
        && boundary.getMinLong() >= minLong
        && boundary.getMaxLong() <= maxLong;
  }

  private String convertToGeoJson(List<Region> regions) {
    // Convert the list of Region objects back into GeoJSON using Moshi
    Moshi moshi = new Moshi.Builder().build();
    JsonAdapter<List<Region>> jsonAdapter =
        moshi.adapter(Types.newParameterizedType(List.class, Region.class));

    return jsonAdapter.toJson(regions);
  }
}
