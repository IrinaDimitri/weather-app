package com.example.controller;

import com.example.model.Weather;
import com.example.repository.WeatherRepository;
import com.example.service.WeatherService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.List;

@RestController
@Tag(name = "Weather API", description = "Управление данными о погоде")
public class WeatherController {

    private final WeatherRepository weatherRepository;
    private final WeatherService weatherService;
    private static final Logger log = LoggerFactory.getLogger(WeatherController.class);

    public WeatherController(WeatherRepository weatherRepository, WeatherService weatherService) {
        this.weatherRepository = weatherRepository;
        this.weatherService = weatherService;
    }

    @GetMapping(value = "/ping", produces = MediaType.TEXT_HTML_VALUE)
    public ResponseEntity<String> ping() {
        log.info("Ping запрос");
        return ResponseEntity.ok("<html><body><h1>PONG</h1></body></html>");
    }

    @GetMapping(value = "/health", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> health() {
        log.info("Запрос состояния сервиса");
        return ResponseEntity.ok("{\"status\": \"HEALTHY\"}");
    }

    @GetMapping(value = "/list", produces = MediaType.TEXT_HTML_VALUE)
    public ResponseEntity<String> list() {
        log.info("Запрос списка всех городов с погодой");
        List<Weather> weatherList = weatherRepository.findAll();
        if (weatherList.isEmpty()) {
            log.warn("Список городов пуст");
            return ResponseEntity.status(HttpStatus.NO_CONTENT)
                .body("<html><body><h2>Информация</h2><p>Список городов с погодой пока пуст.</p></body></html>");
        }

        StringBuilder html = new StringBuilder("<html><body><ul>");
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
        summary = "Добавить или обновить данные о погоде",
        description = "Добавляет новый город или обновляет данные о погоде существующего города в базе данных",
        responses = {
            @ApiResponse(
                responseCode = "200",
                description = "Данные успешно добавлены/обновлены",
                content = @Content(mediaType = "text/html")
            ),
            @ApiResponse(
                responseCode = "400",
                description = "Неверные параметры запроса"
            ),
            @ApiResponse(
                responseCode = "500",
                description = "Внутренняя ошибка сервера"
            )
        }
    )
    @PostMapping(value = "/add", produces = MediaType.TEXT_HTML_VALUE)
    public ResponseEntity<String> add(
        @RequestParam String city,
        @RequestParam(required = false) Integer temperature,
        @RequestParam(defaultValue = "false") boolean forceUpdate
    ) {
        log.info("Запрос добавления или обновления данных для города: {}", city);

        if (city == null || city.trim().isEmpty()) {
            String msg = "Параметр 'city' не должен быть пустым.";
            log.error(msg);
            return ResponseEntity.badRequest().body(errorHtml(msg));
        }
        if (!city.matches("[a-zA-Z\\s-]+")) {
            String msg = "Название города должно содержать только латинские буквы, пробелы или дефисы.";
            log.error(msg + " Получено: " + city);
            return ResponseEntity.badRequest().body(errorHtml(msg));
        }

        try {
            int tempToSave;
            String updateInfo;

            if (temperature != null) {
                tempToSave = temperature;
                updateInfo = "<p>Температура установлена вручную.</p>";
            } else {
                // Здесь можно интегрировать вызов внешнего API или сервиса для получения температуры
                tempToSave = 20; // заглушка
                updateInfo = "<p>Температура не указана. Использовано значение по умолчанию: 20°C.</p>";
            }

            boolean cityExists = weatherRepository.existsById(city);
            if (cityExists && !forceUpdate) {
                String msg = "Город \"" + city + "\" уже существует в базе данных. " +
                             "Для обновления данных используйте параметр forceUpdate=true.";
                log.warn(msg);
                return ResponseEntity.status(HttpStatus.CONFLICT).body(errorHtml(msg));
            }

            weatherRepository.save(new Weather(city, tempToSave));
            log.info("Город {} успешно {} с температурой {}°C", city, cityExists ? "обновлен" : "добавлен", tempToSave);

            return ResponseEntity.ok(
                "<html><body><h2>Успех!</h2>" +
                "<p>Город \"" + escapeHtml(city) + "\" " +
                (cityExists ? "обновлен" : "добавлен") +
                " с температурой " + tempToSave + "°C.</p>" +
                updateInfo +
                "</body></html>"
            );

        } catch (Exception e) {
            String errorMsg = "Внутренняя ошибка сервера при сохранении данных: " + e.getMessage();
            log.error(errorMsg, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorHtml(errorMsg));
        }
    }

    @Operation(
        summary = "Обновить данные о погоде для города",
        description = "Обновляет данные о погоде для указанного города из внешнего API и сохраняет в базе данных.",
        responses = {
            @ApiResponse(
                responseCode = "200",
                description = "Данные о погоде успешно обновлены",
                content = @Content(mediaType = "application/json")
            ),
            @ApiResponse(
                responseCode = "404",
                description = "Город не найден в базе данных и/или в Open Weather API",
                content = @Content(mediaType = "application/json")
            )
        }
    )

    @PostMapping(value = "/update", produces = MediaType.TEXT_HTML_VALUE)
    public ResponseEntity<String> updateWeather(
        @RequestParam(required = false) String city
    ) {
        log.info("Запрос обновления данных о погоде для города: {}", city);
        try {
            if (city == null || city.trim().isEmpty()) {
                weatherService.updateAllCities();
                return ResponseEntity.ok(
                    "<html><body><h2>Успех!</h2><p>Данные о погоде для всех городов успешно обновлены.</p></body></html>");
            } else {
                boolean updated = weatherService.updateCityWeather(city);
                if (!updated) {
                    String msg = "Город \"" + city + "\" не найден в базе данных.";
                    log.warn(msg);
                    return ResponseEntity.status(HttpStatus.NOT_FOUND)
                            .body(errorHtml(msg));
                }
                log.info("Данные о погоде для города {} успешно обновлены", city);
                return ResponseEntity.ok(
                    "<html><body><h2>Успех!</h2><p>Данные о погоде для города \"" + escapeHtml(city) + "\" успешно обновлены.</p></body></html>");
            }
        } catch (Exception e) {
            String errorMsg = "Внутренняя ошибка сервера при обновлении данных: " + e.getMessage();
            log.error(errorMsg, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(errorHtml(errorMsg));
        }
    }

    @DeleteMapping(value = "/delete", produces = MediaType.TEXT_HTML_VALUE)
    public ResponseEntity<String> delete(
        @RequestParam(required = false) String city,
        @RequestParam(defaultValue = "false") boolean all
    ) {
        log.info("Запрос удаления данных для города: {}, удаление всех данных: {}", city, all);
        try {
            if (all) {
                weatherRepository.deleteAll();
                log.info("Все данные о погоде успешно удалены");
                return ResponseEntity.ok(
                    "<html><body><h2>Успех!</h2><p>Все данные о погоде успешно удалены.</p></body></html>");
            }

            if (city == null || city.trim().isEmpty()) {
                String msg = "Для удаления конкретного города необходимо указать параметр 'city', " +
                             "или установите параметр all=true для удаления всех данных.";
                log.error(msg);
                return ResponseEntity.badRequest().body(errorHtml(msg));
            }

            if (!weatherRepository.existsById(city)) {
                String msg = "Город \"" + city + "\" не найден в базе данных, удаление невозможно.";
                log.warn(msg);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorHtml(msg));
            }

            weatherRepository.deleteById(city);
            log.info("Город {} успешно удален", city);
            return ResponseEntity.ok(
                "<html><body><h2>Успех!</h2><p>Город \"" + escapeHtml(city) + "\" успешно удален.</p></body></html>");
        } catch (Exception e) {
            String errorMsg = "Внутренняя ошибка сервера при удалении данных: " + e.getMessage();
            log.error(errorMsg, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorHtml(errorMsg));
        }
    }

    private String errorHtml(String message) {
        return "<html><body><h2>Ошибка!</h2><p>" + escapeHtml(message) + "</p></body></html>";
    }

    private String escapeHtml(String input) {
        if (input == null) return "";
        return input.replace("&", "&amp;")
                    .replace("<", "&lt;")
                    .replace(">", "&gt;")
                    .replace("\"", "&quot;")
                    .replace("'", "&#39;");
    }
}
