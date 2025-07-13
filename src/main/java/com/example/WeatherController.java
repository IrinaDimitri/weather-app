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
import org.springframework.http.HttpStatus;
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
        try {
            weatherService.updateAllCities();
            return ResponseEntity.ok("Weather data updated successfully");
        } catch (UnsupportedOperationException e) {
            return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED)
                .body("Weather update functionality is not implemented");
        }
    }

    @PostMapping(value = "/add", produces = MediaType.TEXT_HTML_VALUE)
    public ResponseEntity<String> add(
        @RequestParam String city,
        @RequestParam(required = false) Integer temperature,
        @RequestParam(defaultValue = "false") boolean forceUpdate
    ) {
        if (city == null || city.trim().isEmpty() || !city.matches("[a-zA-Z\\s-]+")) {
            return ResponseEntity.badRequest().body(
                errorHtml("Название города должно содержать только буквы и дефисы.")
            );
        }

        try {
            int tempToSave;
            String updateInfo = "";
            
            if (temperature != null) {
                tempToSave = temperature;
                updateInfo = "<p>Температура задана вручную. Для обновления используйте метод /update.</p>";
            } else {
                try {
                    // Проверяем доступность сервиса
                    if (weatherService == null) {
                        throw new UnsupportedOperationException("Weather service not available");
                    }
                    tempToSave = 20; // Значение по умолчанию, если сервис не реализован
                    updateInfo = "<p>Автоматическое получение температуры не реализовано. Используйте параметр temperature.</p>";
                } catch (Exception e) {
                    tempToSave = 20;
                    updateInfo = "<p>Ошибка при получении температуры: " + e.getMessage() + "</p>";
                }
            }

            if (weatherRepository.existsById(city) && !forceUpdate) {
                return ResponseEntity.badRequest().body(
                    errorHtml("Город \"" + city + "\" уже есть в базе. Используйте ?forceUpdate=true, чтобы обновить данные.")
                );
            }

            weatherRepository.save(new Weather(city, tempToSave));

            return ResponseEntity.ok(
                "<html><body><h2>Успех!</h2>" +
                "<p>Город \"" + escapeHtml(city) + "\" " + 
                (forceUpdate ? "обновлен" : "добавлен") + 
                " с температурой " + tempToSave + "°C.</p>" +
                updateInfo +
                "</body></html>"
            );

        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(
                errorHtml("Ошибка при сохранении данных: " + e.getMessage())
            );
        }
    }

    @Operation(
        summary = "Удалить город или все данные",
        description = "Удаляет конкретный город или все данные из БД",
        responses = {
            @ApiResponse(
                responseCode = "200",
                description = "Удаление выполнено",
                content = @Content(
                    mediaType = "text/html"
                )
            ),
            @ApiResponse(
                responseCode = "400",
                description = "Неверные параметры запроса"
            )
        }
    )
    @DeleteMapping(value = "/delete", produces = MediaType.TEXT_HTML_VALUE)
    public ResponseEntity<String> delete(
        @RequestParam(required = false) String city,
        @RequestParam(defaultValue = "false") boolean all
    ) {
        try {
            if (all) {
                weatherRepository.deleteAll();
                return ResponseEntity.ok(
                    successHtml("Все данные о погоде успешно удалены.")
                );
            }
            
            if (city == null || city.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(
                    errorHtml("Необходимо указать название города или параметр all=true")
                );
            }

            if (!weatherRepository.existsById(city)) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                    errorHtml("Город \"" + city + "\" не найден в базе.")
                );
            }

            weatherRepository.deleteById(city);

            return ResponseEntity.ok(
                successHtml("Город \"" + city + "\" успешно удален.")
            );
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(
                errorHtml("Ошибка при удалении данных: " + e.getMessage())
            );
        }
    }

    private String successHtml(String message) {
        return "<html><body><h2>Успех!</h2><p>" + escapeHtml(message) + "</p></body></html>";
    }

    private String errorHtml(String message) {
        return "<html><body><h2>Ошибка!</h2><p>" + escapeHtml(message) + "</p></body></html>";
    }

    private String escapeHtml(String input) {
        return input.replace("&", "&amp;")
                   .replace("<", "&lt;")
                   .replace(">", "&gt;");
    }
}