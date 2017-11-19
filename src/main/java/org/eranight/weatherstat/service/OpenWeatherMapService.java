package org.eranight.weatherstat.service;

import javafx.util.Pair;
import okhttp3.*;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.*;

@Service
public class OpenWeatherMapService {

    private static Logger logger = LoggerFactory.getLogger(OpenWeatherMapService.class);

    private OkHttpClient client = new OkHttpClient();

    private static final String MEDIA_TYPE = "application/x-www-form-urlencoded";
    private static final String CITY_ID = "{city_id}";
    private static final String MY_APPID = "{MY_APPID}";
    private static final String REQUEST =
            "http://api.openweathermap.org/data/2.5/forecast?id=" + CITY_ID + "&units=metric&APPID=" + MY_APPID;

    public List<Pair<Date, Integer>> getTemps(int cityId, String appId) {
        String fullRequest = getFullRequest(String.valueOf(cityId), appId);
        Request request = new Request.Builder()
                .post(RequestBody.create(MediaType.parse(MEDIA_TYPE), ""))
                .url(fullRequest)
                .build();

        try {
            Response response = client.newCall(request).execute();
            if (response.isSuccessful()) {
                return parseResponse(response.body().string());
            }
        } catch (IOException e) {
            logger.error(e.getMessage());
        }

        return Collections.EMPTY_LIST;
    }

    private String getFullRequest(String cityId, String appId) {
        return REQUEST.replace(CITY_ID, cityId)
                .replace(MY_APPID, appId);
    }

    private List<Pair<Date, Integer>> parseResponse(String response) {
        List<Pair<Date, Integer>> result = new ArrayList<>();
        try {
            JSONObject mainObject = new JSONObject(response);
            JSONArray tempsArray = mainObject.getJSONArray("list");
            for (int index = 0; index < tempsArray.length(); ++index) {
                try {
                    float temp = tempsArray.getJSONObject(index).getJSONObject("main").getFloat("temp");
                    long unixtime = tempsArray.getJSONObject(index).getLong("dt");
                    result.add(new Pair<>(new Date(unixtime * 1000), Math.round(temp)));
                } catch (JSONException e) {
                    logger.error(e.getMessage());
                }
            }
        } catch (JSONException e) {
            logger.error(e.getMessage());
        }
        return result;
    }

}
