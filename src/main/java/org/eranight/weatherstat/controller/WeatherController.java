package org.eranight.weatherstat.controller;

import javafx.util.Pair;
import org.eranight.weatherstat.service.AvailableCitiesService;
import org.eranight.weatherstat.service.OpenWeatherMapService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Controller
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
                .map(pair -> "\"" + pair.getKey().toString() + "\"")
                .collect(Collectors.joining(", "));
        String data = temps.stream()
                .map(pair -> pair.getValue().toString())
                .collect(Collectors.joining(", "));
        return new StringBuilder()
                .append("<canvas id=\"chart\"></canvas>")
                .append("<script>")
                    .append("var ctx = document.getElementById(\"chart\").getContext('2d');")
                    .append("var chart = new Chart(ctx, {")
                        .append("type: \'line\',")
                        .append("data: {")
                            .append("labels: [" + labels + "],")
                            .append("datasets: [{")
                                .append("label: \"temperature \u2103\",")
                                .append("backgroundColor: 'rgba(255, 99, 132, 0.2)',")
                                .append("borderColor: 'rgb(255, 99, 132)',")
                                .append("data: [" + data + "],")
                            .append("}]")
                        .append("},")
                    .append("});")
                .append("</script>")
                .toString();
    }
}
