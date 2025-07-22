package com.example.service;

import com.example.model.Weather;
import com.example.repository.WeatherRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.tags.Tag;  
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@Tag(name = "Weather Service", description = "Сервис для получения и обновления данных о погоде")
public class WeatherService {
    private final WeatherRepository weatherRepo;


    @Autowired
    public WeatherService(WeatherRepository weatherRepo) {
        this.weatherRepo = weatherRepo;  
    }


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
}
