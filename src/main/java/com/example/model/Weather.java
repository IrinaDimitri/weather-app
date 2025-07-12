package com.example.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "weather")
public class Weather {
    @Id
    private String city;
    private int temperature;

// Конструкторы
    public Weather() {}

    public Weather(String city, int temperature) {
        this.city = city;
        this.temperature = temperature;
    }

// Геттеры и сеттеры
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