package edu.brown.cs.student.main.server.handlers;

import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.Moshi;
import com.squareup.moshi.Types;
import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import spark.Request;
import spark.Response;
import spark.Route;

public class GeoHandler implements Route {

  public record Region(String type, Coords coords, Props props) {}

  public record Coords(String type, List<List<List<List<Double>>>> coordinates) {}
  public record Props (String state, String city, String name, String holc_grade, Map<String,
          String> area_description_data) {}

  @Override
  public Object handle(Request request, Response response) {
    try {
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

      // Return GeoJSON data
      return new GeoJsonSuccessResponse(filteredRegions).serialize();
    } catch (Exception e) {
      List<Region> regions = loadGeoJsonData();
      // Convert back to GeoJSON
      // Return GeoJSON data
      return new GeoJsonSuccessResponse(regions).serialize();
    }
  }

  private List<Region> loadGeoJsonData() {
    try {
      String filepath =
          "server/data/fullDownload.json";
      // Load the GeoJSON data from a file
      String geoJson = new String(Files.readAllBytes(Paths.get(filepath)));

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

    List<List<List<List<Double>>>> coords = region.coords().coordinates;
    return true;


  }

//  private String convertToGeoJson(List<Region> regions) {
//    // Convert the list of Region objects back into GeoJSON using Moshi
//    Moshi moshi = new Moshi.Builder().build();
//    JsonAdapter<List<Region>> jsonAdapter =
//        moshi.adapter(Types.newParameterizedType(List.class, Region.class));
//
//    return jsonAdapter.toJson(regions);
//  }

  private record GeoJsonSuccessResponse(String type, List<Region> filteredRegions) {
    public GeoJsonSuccessResponse(List<Region> filteredRegions) {
      this("success", filteredRegions);
    }

    String serialize() {

      Map<String, Object> response = new HashMap();
      response.put("result", this.type);
      response.put("filtered features", this.filteredRegions);

      Type mapOfStringObjectType = Types.newParameterizedType(Map.class, String.class, Object.class);

      try {
        Moshi moshi = new Moshi.Builder().build();
        JsonAdapter<Map<String, Object>> adapter = moshi.adapter(mapOfStringObjectType);
        String json = adapter.toJson(response);
        return json;
      } catch(Exception e) {
        e.printStackTrace();
        throw e;
      }
    }
  }

  private record GeoJsonFailureResponse(String type, String error) {
    public GeoJsonFailureResponse(String error) {
      this("failure", error);
    }

    String serialize() {

      Map<String, Object> response = new HashMap();
      response.put("result", this.type);
      response.put("filtered features", this.error);

      Type mapOfStringObjectType = Types.newParameterizedType(Map.class, String.class, Object.class);

      try {
        Moshi moshi = new Moshi.Builder().build();
        JsonAdapter<Map<String, Object>> adapter = moshi.adapter(mapOfStringObjectType);
        String json = adapter.toJson(response);
        return json;
      } catch(Exception e) {
        e.printStackTrace();
        throw e;
      }
    }
  }


}
