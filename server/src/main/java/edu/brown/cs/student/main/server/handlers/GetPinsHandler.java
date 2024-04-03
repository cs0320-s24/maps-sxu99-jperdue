package edu.brown.cs.student.main.server.handlers;

import edu.brown.cs.student.main.server.storage.StorageInterface;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import spark.Request;
import spark.Response;
import spark.Route;

public class GetPinsHandler implements Route {

    public StorageInterface storageHandler;

    public GetPinsHandler(StorageInterface storageHandler) {
        this.storageHandler = storageHandler;
    }

    /**
     * Invoked when a request is made on this route's corresponding path e.g. '/hello'
     *
     * @param request The request object providing information about the HTTP request
     * @param response The response object providing functionality for modifying the response
     * @return The content to be set in the response
     */
    @Override
    public Object handle(Request request, Response response) {
        Map<String, Object> responseMap = new HashMap<>();
        try {
            String uid = request.queryParams("uid");

            System.out.println("getting pins for user: " + uid);

            // get all the words for the user
            List<Map<String, Object>> vals = this.storageHandler.getCollection(uid, "pins");

            System.out.println(vals);

            // convert the key,value map to just a list of the words.
            List<String> pins = vals.stream().map(pin -> pin.get("lat").toString() + "-" + pin.get("long").toString()).toList();

            responseMap.put("response_type", "success");
            responseMap.put("pins", pins);
        } catch (Exception e) {
            // error likely occurred in the storage handler
            e.printStackTrace();
            responseMap.put("response_type", "failure");
            responseMap.put("error", e.getMessage());
        }

        return Utils.toMoshiJson(responseMap);
    }
}
