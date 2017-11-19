package org.eranight.weatherstat;

import org.eranight.weatherstat.service.AvailableCitiesService;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Controller;

@SpringBootApplication
public class WeatherStatApplication {

    public static void main(String[] args) {
        SpringApplication.run(WeatherStatApplication.class, args);
    }

    @Controller
    public static class Initializator implements BeanPostProcessor {

        @Override
        public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
            return bean;
        }

        @Override
        public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
            if (bean instanceof AvailableCitiesService) {
                ((AvailableCitiesService) bean).loadCitiesList();
            }
            return bean;
        }
    }

}
