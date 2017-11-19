package org.eranight.weatherstat.service;

import okhttp3.*;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Service
public class OpenWeatherMapService {

    private static Logger logger = LoggerFactory.getLogger(OpenWeatherMapService.class);

    private OkHttpClient client = new OkHttpClient();

    private static final String MEDIA_TYPE = "application/x-www-form-urlencoded";
    private static final String CITY_ID = "{city_id}";
    private static final String MY_APPID = "{MY_APPID}";
    private static final String REQUEST =
            "http://api.openweathermap.org/data/2.5/forecast?id=" + CITY_ID + "&units=metric&APPID=" + MY_APPID;

    public List<Integer> getTemps(int cityId, String appId) {
        logger.info(cityId + " " + appId);
        String fullRequest = getFullRequest(String.valueOf(cityId), appId);
        logger.info(fullRequest);
        Request request = new Request.Builder()
                .post(RequestBody.create(MediaType.parse(MEDIA_TYPE), ""))
                .url(fullRequest)
                .build();

        try {
            Response response = client.newCall(request).execute();
            if (response.isSuccessful()) {
                return parseResponse(response.body().string());
            } else {
                logger.info(response.message());
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

    private List<Integer> parseResponse(String response) {
        logger.info(response);
        List<Integer> result = new ArrayList<>();
        try {
            JSONObject mainObject = new JSONObject(response);
            JSONArray tempsArray = mainObject.getJSONArray("list");
            for (int index = 0; index < tempsArray.length(); ++index) {
                try {
                    float temp = tempsArray.getJSONObject(index).getJSONObject("main").getFloat("temp");
                    result.add(Math.round(temp));
                } catch (JSONException e) {

                }
            }
        } catch (JSONException e) {

        }
        return result;
    }

}
