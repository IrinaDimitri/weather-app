package com.example.model;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "weather")
@Schema(description = "Модель данных о погоде в городе")
public class Weather {
    @Id
    @Schema(description = "Название города", example = "Berlin")
    private String city;
    
    @Schema(description = "Температура в градусах Цельсия", example = "20")
    private int temperature;

    public Weather() {}

    public Weather(String city, int temperature) {
        this.city = city;
        this.temperature = temperature;
    }

    // Геттеры и сеттеры остаются без изменений
    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public int getTemperature() {
        return temperature;
    }

    public void setTemperature(int temperature) {
        this.temperature = temperature;
    }
}