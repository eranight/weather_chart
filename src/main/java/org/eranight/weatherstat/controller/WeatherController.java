package org.eranight.weatherstat.controller;

import org.eranight.weatherstat.service.AvailableCitiesService;
import org.eranight.weatherstat.service.OpenWeatherMapService;
import org.eranight.weatherstat.util.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@CrossOrigin
@RequestMapping("weather")
public class WeatherController {
    private static Logger logger = LoggerFactory.getLogger(WeatherController.class);

    private static final String MESSAGE = "{message}";
    private static final String BAD_RESPONSE = "Oops, something wrong!";

    @Autowired
    OpenWeatherMapService openWeatherMapService;
    @Autowired
    AvailableCitiesService availableCitiesService;
    @Value("${APPID}")
    private String appId;

    @RequestMapping(
            path = "stat",
            method = RequestMethod.POST,
            consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE
    )
    @ResponseStatus(value = HttpStatus.OK)
    public ResponseEntity<String> stat(
            @RequestParam("cityName") String cityName
    ) {
        int cityId = availableCitiesService.getId(cityName);
        if (cityId == -1) {
            return ResponseEntity.badRequest().body(BAD_RESPONSE);
        }
        if ("{YOUR_APPID}".equals(appId)) {
            logger.error("You must put your APPID to the APPID.properties file!");
            return ResponseEntity.badRequest().body(BAD_RESPONSE);
        }
        List<Pair<Date, Integer>> temps = openWeatherMapService.getTemps(cityId, appId);
        if (temps.isEmpty()) {
            logger.warn("the response list is empty, something wrong");
            return ResponseEntity.badRequest().body(BAD_RESPONSE);
        } else {
            String answer = temps.stream().map(String::valueOf).collect(Collectors.joining("\n"));
            logger.debug("good response");
            return ResponseEntity.ok().body(buildChart(temps));
        }
    }

    @RequestMapping(
            path = "cities",
            method = RequestMethod.GET,
            produces = MediaType.TEXT_PLAIN_VALUE
    )
    public ResponseEntity<String> cities() {
        return ResponseEntity.ok().body(availableCitiesService.getOneStringCitiesNames(", "));
    }

    private String buildChart(List<Pair<Date, Integer>> temps) {
        String labels = temps.stream()
                .map(pair -> "\"" + pair.getFirst().toString() + "\"")
                .collect(Collectors.joining(", "));
        String data = temps.stream()
                .map(pair -> pair.getSecond().toString())
                .collect(Collectors.joining(", "));
        return CANVAS_SCRIPT.replace(LABELS_LABEL, labels).replace(DATA_LABEL, data);
    }
    
    private static final String LABELS_LABEL = "{LABELS}";
    private static final String DATA_LABEL = "{DATA}";
    private static final String CANVAS_SCRIPT =
    		"<canvas id=\"chart\"></canvas>" +
    		"<script>" +
    			"var ctx = document.getElementById(\"chart\").getContext('2d');" +
    			"var chart = new Chart(ctx, {" +
    				"type: \'line\'," +
    				"data: {" +
    					"labels: [" + LABELS_LABEL + "]," +
    					"datasets: [{" +
    						"label: \"temperature \u2103\"," +
    						"backgroundColor: 'rgba(255, 99, 132, 0.2)'," +
    						"borderColor: 'rgb(255, 99, 132)'," +
    						"data: [" + DATA_LABEL + "]," +
    					"}]" +
    				"}," +
    			"});" +
    		"</script>";
}
