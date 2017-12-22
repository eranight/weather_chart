package org.eranight.weatherstat.service;

import org.eranight.weatherstat.util.Pair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class AvailableCitiesService {

    private static Logger logger = LoggerFactory.getLogger(AvailableCitiesService.class);

    private List<Pair<Integer, String>> cities;

    @PostConstruct
    public void loadCitiesList() {
        cities = new ArrayList<>();
        String jsonString = "";
        try {
            List<String> lines = Files.readAllLines(
                    Paths.get(getClass().getClassLoader().getResource("cities.json").toURI()));
            jsonString = lines.stream().collect(Collectors.joining());
        } catch (IOException e) {
            logger.error("IO exception");
        } catch (URISyntaxException e) {
            logger.error("URI Syntax exception");
        }

        if (!"".equals(jsonString)) {
            try {
                JSONObject mainObject = new JSONObject(jsonString);
                JSONArray citiesArray = mainObject.getJSONArray("cities");
                for (int index = 0; index < citiesArray.length(); ++index) {
                    try {
                        JSONObject cityJson = citiesArray.getJSONObject(index);
                        cities.add(new Pair<>(cityJson.getInt("id"), cityJson.getString("name")));
                    } catch (JSONException e) {
                        logger.error(e.getMessage());
                    }
                }
            } catch (JSONException e) {
                logger.error(e.getMessage());
            }
        }
    }

    public List<String> getCitiesNames() {
        return cities.stream().map(Pair::getSecond).collect(Collectors.toList());
    }

    public String getOneStringCitiesNames(String delimeter) {
        return cities.stream().map(Pair::getSecond).collect(Collectors.joining(delimeter));
    }

    public int getId(String name) {
        return cities.stream().filter(
                pair -> pair.getSecond().equals(name)).findFirst().orElse(new Pair<>(-1, null)).getFirst();
    }
}
