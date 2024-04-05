package edu.brown.cs.student.main.server.handlers;

import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.Moshi;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;
import spark.Request;
import spark.Response;
import spark.Route;

public class GeoHandler implements Route {

  public GeoHandler(String filepath) {
    this.filepath = filepath;
  }

  public final String filepath;
  public GeoJsonData geoJsonData;

  /**
   * Handle GeoJson request
   * @param request url with params
   * @param response response object
   * @return Object
   */
  @Override
  public Object handle(Request request, Response response) {
    try { // Handle when keyword is provided from search in the backend
      if (request.queryParams("keyword") != null){
        String keyword = request.queryParams("keyword");
        this.loadGeoJsonData();
        List<GeoJsonData.Feature> features = this.geoJsonData.features;
        // Filter regions
        List<GeoJsonData.Feature> filteredRegions =
                features.stream()
                        .filter(feature -> this.hasKeyword(feature, keyword))
                        .collect(Collectors.toList());

        GeoJsonData output = new GeoJsonData(this.geoJsonData.type, filteredRegions);

        Map<String, Object> responseMap = new HashMap<String, Object>();
        responseMap.put("data", output);

        // Return GeoJSON data
        return Utils.toMoshiJson(responseMap);
      } else {

        double minLat = Double.parseDouble(request.queryParams("minLat"));
        double maxLat = Double.parseDouble(request.queryParams("maxLat"));
        double minLong = Double.parseDouble(request.queryParams("minLong"));
        double maxLong = Double.parseDouble(request.queryParams("maxLong"));

        // Load GeoJSON data
        try {
          this.loadGeoJsonData();
          List<GeoJsonData.Feature> features = this.geoJsonData.features;

          // Filter regions
          List<GeoJsonData.Feature> filteredRegions =
                  features.stream()
                          .filter(feature -> isContainedIn(feature, minLat, maxLat, minLong, maxLong))
                          .collect(Collectors.toList());

          GeoJsonData output = new GeoJsonData(this.geoJsonData.type, filteredRegions);

          Map<String, Object> responseMap = new HashMap<String, Object>();
          responseMap.put("data", output);

          // Return GeoJSON data
          return Utils.toMoshiJson(responseMap);
          // return new GeoJsonSuccessResponse(output).serialize();
        } catch (IOException e) {
          return new GeoJsonFailureResponse(e.getMessage()).serialize();
        }
      }
    } catch (Exception e) {
      try {
        loadGeoJsonData();
        // Convert back to GeoJSON
        // Return GeoJSON data
        Map<String, Object> responseMap = new HashMap<String, Object>();
        responseMap.put("data", this.geoJsonData);
        return Utils.toMoshiJson(responseMap);

      } catch (Exception e2) {
        return new GeoJsonFailureResponse(e2.getMessage()).serialize();
      }
    }
  }

  /**
   * Parses JSON data from a JsonReader and converts it to the specified target type.
   *
   * @param source The JsonReader containing the JSON data.
   * @param targetType The Class representing the target data type to convert the JSON to.
   * @param <T> The generic type of the target data.
   * @return An instance of the target data type parsed from the JSON.
   * @throws IOException if there's an error reading or parsing the JSON data.
   */
  public static <T> T fromJsonGeneral(String source, Class<T> targetType) throws IOException {
    Moshi moshi = new Moshi.Builder().build();
    JsonAdapter<T> adapter = moshi.adapter(targetType);
    //    source.setLenient(true);

    return adapter.fromJson(source);
  }

  /**
   * Load Json data
   *
   * @return GeoJsonData
   */
  public void loadGeoJsonData() throws IOException {
    try {
      FileReader jsonReader = new FileReader(this.filepath);
      BufferedReader br = new BufferedReader(jsonReader);
      String fileString = "";
      String line = br.readLine();
      while (line != null) {
        fileString = fileString + line;
        line = br.readLine();
      }
      jsonReader.close();

      this.geoJsonData = fromJsonGeneral(fileString, GeoJsonData.class);

    } catch (IOException e) {
      System.out.println(e.getMessage());
      throw e;
    }
  }

  private boolean isContainedIn(
      GeoJsonData.Feature feature, double minLat, double maxLat, double minLng, double maxLng) {
    // Get the boundary of the region

    Double regMinLat = 90.0;
    Double regMaxLat = -90.0;
    Double regMinLng = 180.0;
    Double regMaxLng = -180.0;

    List<List<List<List<Double>>>> coords = feature.geometry.coordinates;
    for (List<List<List<Double>>> poly : coords) {
      for (List<List<Double>> ring : poly) {
        for (List<Double> coordinate : ring) {
          regMinLat = Double.min(regMinLat, coordinate.get(1));
          regMaxLat = Double.max(regMaxLat, coordinate.get(1));
          regMinLng = Double.min(regMinLng, coordinate.get(0));
          regMaxLng = Double.max(regMaxLng, coordinate.get(0));
        }
      }
    }

    return (regMinLat >= minLat
        && regMaxLat <= maxLat
        && regMinLng >= minLng
        && regMaxLng <= maxLng);
  }

  /**
   * Function to check if a feature has desired keyword
   * @param feature feature being observed
   * @param keyword word to search for
   * @return true if contains word
   */
  private boolean hasKeyword(GeoJsonData.Feature feature, String keyword) {

    Collection<String> area_description_data = feature.properties.area_description_data.values();

    for (String word : area_description_data){
      String[] split = word.split(" ");
      if (Arrays.stream(split).toList().contains(keyword)){
        return true;
      }
    }
    return false;
  }

  private record GeoJsonFailureResponse(String type, String error) {
    public GeoJsonFailureResponse(String error) {
      this("failure", error);
    }

    String serialize() {

      Map<String, Object> response = new HashMap();
      response.put("result", this.type);
      response.put("message", this.error);

      try {
        return Utils.toMoshiJson(response);
      } catch (Exception e) {
        e.printStackTrace();
        throw e;
      }
    }
  }
}
