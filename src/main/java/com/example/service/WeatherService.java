package com.example.service;

import com.example.model.Weather;
import com.example.repository.WeatherRepository;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Map;

@Slf4j
@Service
public class WeatherService {
    private static final String API_URL = "https://api.open-meteo.com/v1/forecast";
    private final WeatherRepository weatherRepo;
    
    private static final Map<String, double[]> CITIES = Map.of(
        "Berlin", new double[]{52.52, 13.41},
        "London", new double[]{51.51, -0.13},
        "Paris", new double[]{48.85, 2.35}
    );

    @Autowired
    public WeatherService(WeatherRepository weatherRepo) {
        this.weatherRepo = weatherRepo;
    }

    @PostConstruct
    @Scheduled(fixedRateString = "${weather.update.interval}")
    public void updateAllCities() {
        weatherRepo.findAll().forEach(weather -> {
            try {
                updateCityWeather(weather.getCity());
            } catch (Exception e) {
                log.error("Ошибка при обновлении города {}: {}", weather.getCity(), e.getMessage());
                saveDefaultTemp(weather.getCity());
            }
        });
    }

    private void updateCityWeather(String city) throws Exception {
        double[] coords = getCityCoordinates(city);
        double temperature = fetchCurrentTemperature(coords[0], coords[1]);
        Weather weather = weatherRepo.findById(city).orElse(new Weather(city, 0));
        weather.setTemperature((int) Math.round(temperature));
        weatherRepo.save(weather);
    }

    private void saveDefaultTemp(String city) {
        Weather weather = weatherRepo.findById(city).orElse(new Weather(city, 0));
        weather.setTemperature(20); // Значение по умолчанию
        weatherRepo.save(weather);
    }

    public double[] getCityCoordinates(String city) {
        if (CITIES.containsKey(city)) {
            return CITIES.get(city);
        }
        throw new IllegalArgumentException("Город не найден: " + city);
    }

    public double fetchCurrentTemperature(double latitude, double longitude) throws Exception {
        RestTemplate restTemplate = new RestTemplate();
        String url = String.format("%s?latitude=%f&longitude=%f&current_weather=true", API_URL, latitude, longitude);
        String response = restTemplate.getForObject(url, String.class);
        
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree(response);
        return root.path("current_weather").path("temperature").asDouble();
    }
}