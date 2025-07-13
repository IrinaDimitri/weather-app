package com.example.service;
import org.springframework.scheduling.annotation.Scheduled;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import com.example.model.Weather;
import com.example.repository.WeatherRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Map;

@Slf4j
@Service
@Tag(name = "Weather Service", description = "Сервис для получения и обновления данных о погоде")
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

    // @PostConstruct
    // @Scheduled(fixedRateString = "${weather.update.interval}")
    //     public void scheduledUpdate() {
    //     log.info("Запуск обновления данных по расписанию");
    //     updateAllCities();
    // }
    // public void updateAllCities() {
    //     weatherRepo.findAll().forEach(weather -> {
    //         log.info("Обновление данных для всех городов...");
    //         try {
    //             updateCityWeather(weather.getCity());
    //         } catch (Exception e) {
    //             log.error("Ошибка при обновлении города {}: {}", weather.getCity(), e.getMessage());
    //             saveDefaultTemp(weather.getCity());
    //         }
    //     });
    // }

    // @Operation(
    //     summary = "Обновить данные о погоде для города",
    //     description = "Обновляет данные о погоде для указанного города из внешнего API и сохраняет в базе данных.",
    //     responses = {
    //         @ApiResponse(
    //             responseCode = "200",
    //             description = "Данные о погоде успешно обновлены",
    //             content = @Content(mediaType = "application/json")
    //         ),
    //         @ApiResponse(
    //             responseCode = "404",
    //             description = "Город не найден в базе данных",
    //             content = @Content(mediaType = "application/json")
    //         )
    //     }
    // )
    // public boolean updateCityWeather(String city) throws Exception {
    //     double[] coords = getCityCoordinates(city);
    //     double temperature = fetchCurrentTemperature(coords[0], coords[1]);
    //     Weather weather = weatherRepo.findById(city).orElse(new Weather(city, 0));
    //     weather.setTemperature((int) Math.round(temperature));
    //     weatherRepo.save(weather);
    //     return true;
    // }

    @Operation(
        summary = "Обновить данные о погоде для всех городов",
        description = "Обновляет данные о погоде для всех городов в базе данных.",
        responses = {
            @ApiResponse(
                responseCode = "200",
                description = "Данные о погоде успешно обновлены для всех городов",
                content = @Content(mediaType = "application/json")
            ),
            @ApiResponse(
                responseCode = "500",
                description = "Ошибка обновления данных",
                content = @Content(mediaType = "application/json")
            )
        }
    )

    private void saveDefaultTemp(String city) {
        Weather weather = weatherRepo.findById(city).orElse(new Weather(city, 0));
        weather.setTemperature(20); // Значение по умолчанию
        weatherRepo.save(weather);
    }

    private double[] getCityCoordinates(String city) {
        if (CITIES.containsKey(city)) {
            return CITIES.get(city);
        }
        throw new IllegalArgumentException("Город не найден: " + city);
    }

    @Operation(
        summary = "Получить текущую температуру для города",
        description = "Получает текущую температуру для указанного города через API Open-Meteo.",
        responses = {
            @ApiResponse(
                responseCode = "200",
                description = "Температура успешно получена",
                content = @Content(mediaType = "application/json")
            ),
            @ApiResponse(
                responseCode = "500",
                description = "Ошибка при получении температуры",
                content = @Content(mediaType = "application/json")
            )
        }
    )
    public double fetchCurrentTemperature(double latitude, double longitude) throws Exception {
        RestTemplate restTemplate = new RestTemplate();
        String url = String.format("%s?latitude=%f&longitude=%f&current_weather=true", API_URL, latitude, longitude);
        String response = restTemplate.getForObject(url, String.class);

        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree(response);
        return root.path("current_weather").path("temperature").asDouble();
    }
}
