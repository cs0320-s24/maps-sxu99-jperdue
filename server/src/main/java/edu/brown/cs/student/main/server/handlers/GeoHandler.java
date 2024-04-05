package edu.brown.cs.student.main.server.handlers;

import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.Moshi;
import com.squareup.moshi.Types;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.Buffer;
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

  public GeoHandler(String filepath){
    this.filepath = filepath;
  }

  private final String filepath;

  public record GeoJsonData(String type, List<Region> regions) {}
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
      try {
        GeoJsonData data = loadGeoJsonData();
        List<Region> regions = data.regions;

        // Filter regions
        List<Region> filteredRegions =
                regions.stream()
                        .filter(region -> isContainedIn(region, minLat, maxLat, minLong, maxLong))
                        .collect(Collectors.toList());

        GeoJsonData output = new GeoJsonData(data.type, filteredRegions);

        // Return GeoJSON data
        return new GeoJsonSuccessResponse(output).serialize();
      } catch (IOException e) {
        return new GeoJsonFailureResponse(e.getMessage()).serialize();
      }

    } catch (Exception e) {
      try {
        GeoJsonData data = loadGeoJsonData();
        // Convert back to GeoJSON
        // Return GeoJSON data
        return new GeoJsonSuccessResponse(data).serialize();
      } catch (IOException e){
        return new GeoJsonFailureResponse(e.getMessage()).serialize();
      }
    }
  }

  private GeoJsonData loadGeoJsonData() throws IOException {
    try {
      // Load the GeoJSON data from a file
      FileReader reader = new FileReader(this.filepath);
      BufferedReader bufferedReader = new BufferedReader(reader);
      String geoJsonString = "";
      String line = bufferedReader.readLine();
      while (line != null) {
        geoJsonString = geoJsonString + line;
        line = bufferedReader.readLine();
      }
      reader.close();

      // Parse it into a list of Region objects using Moshi
      Moshi moshi = new Moshi.Builder().build();
      JsonAdapter<GeoJsonData> jsonAdapter =
              moshi.adapter(Types.newParameterizedType(GeoJsonData.class));

      return jsonAdapter.fromJson(geoJsonString);
    } catch (IOException e) {
      e.printStackTrace();
      throw e;
    }
  }

  private boolean isContainedIn(
      Region region, double minLat, double maxLat, double minLng, double maxLng) {
    // Get the boundary of the region

    Double regMinLat = 90.0;
    Double regMaxLat = -90.0;
    Double regMinLng = 180.0;
    Double regMaxLng = -180.0;

    List<List<List<List<Double>>>> coords = region.coords().coordinates;
    for (List<List<List<Double>>> poly : coords){
      for (List<List<Double>> ring : poly){
        for (List<Double> coordinate : ring){
          regMinLat = Double.min(regMinLat, coordinate.get(1));
          regMaxLat = Double.max(regMaxLat, coordinate.get(1));
          regMinLng = Double.min(regMinLng, coordinate.get(0));
          regMaxLng = Double.max(regMaxLng, coordinate.get(0));
        }
      }
    }

    return (
            regMinLat >= minLat &&
            regMaxLat <= maxLat &&
            regMinLng >= minLng &&
            regMaxLng <= maxLng);
  }

//  private String convertToGeoJson(List<Region> regions) {
//    // Convert the list of Region objects back into GeoJSON using Moshi
//    Moshi moshi = new Moshi.Builder().build();
//    JsonAdapter<List<Region>> jsonAdapter =
//        moshi.adapter(Types.newParameterizedType(List.class, Region.class));
//
//    return jsonAdapter.toJson(regions);
//  }

  private record GeoJsonSuccessResponse(String type, GeoJsonData data) {
    public GeoJsonSuccessResponse(GeoJsonData data) {
      this("success", data);
    }

    String serialize() {

      Map<String, Object> response = new HashMap();
      response.put("result", this.type);
      response.put("data", this.data);

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
