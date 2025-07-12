package com.example.controller;

import com.example.model.Weather;
import com.example.repository.WeatherRepository;
import com.example.service.WeatherService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@Tag(name = "Weather API", description = "Управление данными о погоде")
public class WeatherController {
    
    private final WeatherRepository weatherRepository;
    private final WeatherService weatherService;

    public WeatherController(WeatherRepository weatherRepository, WeatherService weatherService) {
        this.weatherRepository = weatherRepository;
        this.weatherService = weatherService;
    }

    @Operation(
        summary = "Проверка сервера",
        description = "Возвращает PONG для проверки работоспособности",
        responses = {
            @ApiResponse(
                responseCode = "200",
                description = "Успешный ответ",
                content = @Content(
                    mediaType = "text/html",
                    examples = @ExampleObject("<html><body><h1>PONG</h1></body></html>")
                )
            )
        }
    )
    @GetMapping(value = "/ping", produces = MediaType.TEXT_HTML_VALUE)
    public ResponseEntity<String> ping() {
        return ResponseEntity.ok("<html><body><h1>PONG</h1></body></html>");
    }

    @Operation(
        summary = "Статус сервиса",
        description = "Проверка состояния сервиса",
        responses = {
            @ApiResponse(
                responseCode = "200",
                description = "Сервис работает",
                content = @Content(
                    mediaType = "application/json",
                    examples = @ExampleObject("{\"status\": \"HEALTHY\"}")
                )
            )
        }
    )
    @GetMapping(value = "/health", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("{\"status\": \"HEALTHY\"}");
    }

    @Operation(
        summary = "Список городов",
        description = "Возвращает список городов с температурой",
        responses = {
            @ApiResponse(
                responseCode = "200",
                description = "HTML со списком городов",
                content = @Content(
                    mediaType = "text/html"
                )
            )
        }
    )
    @GetMapping(value = "/list", produces = MediaType.TEXT_HTML_VALUE)
    public ResponseEntity<String> list() {
        StringBuilder html = new StringBuilder("<html><body><ul>");
        
        List<Weather> weatherList = weatherRepository.findAll();
        for (Weather weather : weatherList) {
            html.append("<li>")
                .append(escapeHtml(weather.getCity()))
                .append(": ")
                .append(weather.getTemperature())
                .append("°C</li>");
        }
        
        html.append("</ul></body></html>");
        return ResponseEntity.ok(html.toString());
    }

    @Operation(
        summary = "Обновить данные",
        description = "Обновляет данные о погоде для всех городов",
        responses = {
            @ApiResponse(
                responseCode = "200",
                description = "Данные обновлены",
                content = @Content(
                    mediaType = "text/plain",
                    examples = @ExampleObject("Weather data updated successfully")
                )
            )
        }
    )
    @GetMapping("/update")
    public ResponseEntity<String> updateWeather() {
        weatherService.updateAllCities();
        return ResponseEntity.ok("Weather data updated successfully");
    }

    @Operation(
        summary = "Добавить город",
        description = "Добавляет новый город с температурой",
        requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Данные города",
            required = true,
            content = @Content(
                mediaType = "application/x-www-form-urlencoded",
                schema = @Schema(
                    implementation = AddCityRequest.class
                )
            )
        ),
        responses = {
            @ApiResponse(
                responseCode = "200",
                description = "Город добавлен",
                content = @Content(
                    mediaType = "text/html",
                    examples = @ExampleObject("<html><body><p>Город добавлен!</p></body></html>")
                )
            )
        }
    )
    @PostMapping(value = "/add", produces = MediaType.TEXT_HTML_VALUE)
    public ResponseEntity<String> add(
        @Parameter(description = "Название города", example = "Berlin") @RequestParam String city,
        @Parameter(description = "Температура в градусах Цельсия", example = "20") @RequestParam int temperature
    ) {
        Weather weather = new Weather(city, temperature);
        weatherRepository.save(weather);
        
        return ResponseEntity.ok("<html><body><p>Город добавлен!</p></body></html>");
    }

    private String escapeHtml(String input) {
        return input.replace("&", "&amp;")
                   .replace("<", "&lt;")
                   .replace(">", "&gt;");
    }

    @Schema(description = "Модель запроса для добавления города")
    private static class AddCityRequest {
        @Schema(description = "Название города", example = "Berlin", required = true)
        public String city;
        
        @Schema(description = "Температура в градусах Цельсия", example = "20", required = true)
        public int temperature;
    }
}