package com.madeeasy.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@Entity
@AllArgsConstructor
@NoArgsConstructor
public class WeatherDetail {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Add more fields as needed
    // Weather specific details
    private String main;             // Main weather type (e.g., "Clear")
    private String description;      // Description of the weather (e.g., "clear sky")
    private String icon;             // Weather icon (e.g., "01n")

    private Double temp;             // Current temperature (Celsius)
    private Double feelsLike;        // Feels like temperature (Celsius)
    private Integer humidity;        // Humidity in percentage
    private Integer pressure;        // Atmospheric pressure (hPa)
    private Integer cloudiness;      // Cloudiness percentage
    private Double windSpeed;        // Wind speed (m/s)
    private Integer visibility;      // Visibility in meters

    // You can add more fields from your JSON as needed
    @ManyToOne
    @JoinColumn(name = "weather_info_id")
    @JsonIgnore
    private WeatherInfo weatherInfo;  // Link back to WeatherInfo

}
