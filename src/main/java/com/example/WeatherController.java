package com.example.controller;

import com.example.model.Weather;
import com.example.repository.WeatherRepository;
import com.example.service.WeatherService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class WeatherController {
    
    private final WeatherRepository weatherRepository;
    private final WeatherService weatherService;

    public WeatherController(WeatherRepository weatherRepository, WeatherService weatherService) {
        this.weatherRepository = weatherRepository;
        this.weatherService = weatherService;
    }

    @GetMapping(value = "/ping", produces = MediaType.TEXT_HTML_VALUE)
    public ResponseEntity<String> ping() {
        return ResponseEntity.ok("<html><body><h1>PONG</h1></body></html>");
    }

    @GetMapping(value = "/health", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("{\"status\": \"HEALTHY\"}");
    }

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

    @GetMapping("/update")
    public ResponseEntity<String> updateWeather() {
        weatherService.updateAllCities();
        return ResponseEntity.ok("Weather data updated successfully");
    }

    @PostMapping(value = "/add_poyr", produces = MediaType.TEXT_HTML_VALUE)
    public ResponseEntity<String> addCity(
        @RequestParam String city,
        @RequestParam int temperature
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
}