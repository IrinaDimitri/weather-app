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
import com.fasterxml.jackson.core.JsonProcessingException;
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
        log.info("WeatherService инициализирован");
    }

    @PostConstruct
    @Scheduled(fixedRateString = "${weather.update.interval}")
    public void updateAllCities() {
        log.info("Начато обновление данных о погоде для всех городов");
        CITIES.keySet().forEach(city -> {
            try {
                updateCityWeather(city);
            } catch (Exception e) {
                log.error("Ошибка при обновлении данных для города {}: {}", city, e.getMessage());
                saveDefaultTemp(city);
            }
        });
        log.info("Обновление данных завершено");
    }

    public void updateCityWeather(String city) {
        log.debug("Обновление данных для города: {}", city);
        double[] coords = CITIES.get(city);
        if (coords == null) {
            log.warn("Неизвестный город: {}", city);
            throw new IllegalArgumentException("Unknown city");
        }
        
        try {
            double temp = fetchCurrentTemperature(coords[0], coords[1]);
            weatherRepo.save(new Weather(city, (int) Math.round(temp)));
            log.info("Данные для города {} успешно обновлены: {}°C", city, temp);
        } catch (JsonProcessingException e) {
            log.error("Ошибка парсинга JSON для города {}: {}", city, e.getMessage());
            saveDefaultTemp(city);
        } catch (Exception e) {
            log.error("Неожиданная ошибка для города {}: {}", city, e.getMessage());
            saveDefaultTemp(city);
        }
    }

    private double fetchCurrentTemperature(double lat, double lon) throws JsonProcessingException {
        String url = String.format("%s?latitude=%.2f&longitude=%.2f&current_weather=true", 
                         API_URL, lat, lon);
        log.debug("Запрос к API: {}", url);
        
        String response = new RestTemplate().getForObject(url, String.class);
        JsonNode root = new ObjectMapper().readTree(response);
        
        double temp = root.path("current_weather")
                       .path("temperature")
                       .asDouble();
        
        log.debug("Получена температура: {}°C для координат {}, {}", temp, lat, lon);
        return temp;
    }

    private void saveDefaultTemp(String city) {
        int temp = switch(city) {
            case "Berlin" -> 18;
            case "London" -> 20;
            case "Paris" -> 15;
            default -> 10;
        };
        weatherRepo.save(new Weather(city, temp));
        log.warn("Использовано значение по умолчанию для города {}: {}°C", city, temp);
    }
}