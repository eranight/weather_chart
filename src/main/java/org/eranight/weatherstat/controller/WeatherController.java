package org.eranight.weatherstat.controller;

import org.eranight.weatherstat.service.AvailableCitiesService;
import org.eranight.weatherstat.service.OpenWeatherMapService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.io.BufferedReader;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("weather")
public class WeatherController {
    private static Logger logger = LoggerFactory.getLogger(WeatherController.class);

    private static final String MESSAGE = "{message}";
    private static final String HTMLTAGS = "<h2>" + MESSAGE + "</h2>";
    private static final String BAD_RESPONSE = "Oops, something wrong!";

    @Autowired
    OpenWeatherMapService openWeatherMapService;
    @Autowired
    AvailableCitiesService availableCitiesService;

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
        List<String> temps = openWeatherMapService.getTemps(cityId, getAppid());
        if (temps.isEmpty()) {
            logger.warn("the response list is empty, something wrong");
            return ResponseEntity.ok()
                    .body(HTMLTAGS.replace(MESSAGE, BAD_RESPONSE));
        } else {
            String answer = temps.stream().collect(Collectors.joining("\n"));
            return ResponseEntity.ok().body(HTMLTAGS.replace(MESSAGE, answer));
        }
    }

    @RequestMapping(
            path = "cities",
            method = RequestMethod.GET,
            produces = MediaType.TEXT_PLAIN_VALUE
    )
    public ResponseEntity<String> cities() {
        return ResponseEntity.ok().body(availableCitiesService.getOneStringCitiesNames(" "));
    }

    private String getAppid() {
        try (BufferedReader appidReader = Files.newBufferedReader(
                Paths.get(getClass().getClassLoader().getResource("APPID.properties").toURI()))
        ) {
            String parameter = appidReader.readLine();
            logger.info("parameter=" + parameter);
            return parameter != null ? parameter.replace("APPID=", "") : "";
        } catch (IOException e) {
            logger.warn("io exception");
        } catch (URISyntaxException e) {
            logger.warn("uri syntax exception");
        }
        return "";
    }

}
