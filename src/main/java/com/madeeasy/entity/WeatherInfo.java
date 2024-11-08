package com.madeeasy.entity;

import com.madeeasy.entity.PinCodeLocation;
import com.madeeasy.entity.WeatherDetail;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@Entity
@AllArgsConstructor
@NoArgsConstructor
public class WeatherInfo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String pinCode;              // Pin code
    private LocalDate date;              // Date of the weather record
    private String country;              // Country code (e.g., "IN")
    private Integer timezoneOffset;      // Timezone offset (seconds from UTC)

    // If you need to store information like sunrise/sunset directly in WeatherInfo
    private Integer sunriseTime;         // Sunrise time (timestamp)
    private Integer sunsetTime;          // Sunset time (timestamp)

    @ManyToOne
    private PinCodeLocation location;    // Link to the PinCodeLocation entity

    @OneToMany(mappedBy = "weatherInfo", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<WeatherDetail> weatherDetails;  // Link to weather details

}
