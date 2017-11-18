package org.eranight.weatherstat.service;

import okhttp3.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
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

    public List<String> getTemps(int cityId, String appId) {
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

    private List<String> parseResponse(String response) {
        logger.info(response);
        BufferedReader bufferedReader = new BufferedReader(new StringReader(response));

        return Arrays.asList("lol", "kek", "azaza");
    }

}
