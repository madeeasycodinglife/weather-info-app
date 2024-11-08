package com.madeeasy.service.impl;

import com.madeeasy.entity.PinCodeLocation;
import com.madeeasy.entity.WeatherDetail;
import com.madeeasy.entity.WeatherInfo;
import com.madeeasy.repository.PinCodeLocationRepository;
import com.madeeasy.repository.WeatherDetailRepository;
import com.madeeasy.repository.WeatherInfoRepository;
import com.madeeasy.service.WeatherService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Implementation of the WeatherService interface to interact with the OpenWeather API.
 * This service fetches current weather information based on geocoordinates (latitude and longitude).
 */
@Service
@RequiredArgsConstructor
public class WeatherServiceImpl implements WeatherService {

    private final PinCodeLocationRepository pinCodeLocationRepository;
    private final WeatherInfoRepository weatherInfoRepository;
    private final WeatherDetailRepository weatherDetailRepository;
    private final RestTemplate restTemplate;

    private static final String OPEN_WEATHER_API_KEY = "YOUR_API_KEY"; // Replace with your actual OpenWeather API key
    private static final String OPEN_WEATHER_URL = "https://api.openweathermap.org/data/2.5/weather";
    private static final String GEOCODING_URL = "https://api.openweathermap.org/geo/1.0/zip";

    @Override
    public WeatherInfo getWeatherInfo(String pinCode, LocalDate forDate) {
        // Check if weather data already exists in the database for this pin code and date
        Optional<WeatherInfo> cachedWeather = weatherInfoRepository.findByPinCodeAndDate(pinCode, forDate);
        if (cachedWeather.isPresent()) {
            return cachedWeather.get();
        }

        // Fetch or create the PinCodeLocation based on pin code
        PinCodeLocation location = pinCodeLocationRepository.findByPinCode(pinCode)
                .orElseGet(() -> fetchAndSaveLocation(pinCode));

        // Fetch weather data based on location and save it to the database
        WeatherInfo weatherInfo = fetchWeatherData(location, pinCode, forDate);
        return weatherInfoRepository.save(weatherInfo);
    }

    private PinCodeLocation fetchAndSaveLocation(String pinCode) {
        String url = String.format("%s?zip=%s,IN&appid=%s", GEOCODING_URL, pinCode, OPEN_WEATHER_API_KEY);
        ResponseEntity<Map<String, Object>> response = restTemplate.exchange(url,
                org.springframework.http.HttpMethod.GET,
                null,
                new ParameterizedTypeReference<Map<String, Object>>() {
                });
        System.out.println("response : " + response.getBody());
        validateApiResponse(response);

        Map<String, Object> body = response.getBody();
        double latitude = (double) body.get("lat");
        double longitude = (double) body.get("lon");

        PinCodeLocation pinCodeLocation = PinCodeLocation.builder()
                .pinCode(pinCode)
                .latitude(latitude)
                .longitude(longitude)
                .build();

        return pinCodeLocationRepository.save(pinCodeLocation);
    }


    public WeatherInfo fetchWeatherData(PinCodeLocation location, String pinCode, LocalDate forDate) {
        // Build the API request URL
        String url = UriComponentsBuilder.fromHttpUrl(OPEN_WEATHER_URL)
                .queryParam("lat", location.getLatitude())
                .queryParam("lon", location.getLongitude())
                .queryParam("appid", OPEN_WEATHER_API_KEY)
                .queryParam("units", "metric")
                .toUriString();

        try {
            // Fetch weather data using ParameterizedTypeReference
            ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                    url, HttpMethod.GET, null, new ParameterizedTypeReference<Map<String, Object>>() {
                    });

            // Validate the response (you may want to throw an exception here if the response is invalid)
            Map<String, Object> weatherData = response.getBody();
            if (weatherData == null) {
                throw new RuntimeException("Weather data response is empty");
            }

            // Extract main weather information
            Map<String, Object> coord = (Map<String, Object>) weatherData.get("coord");
            double lat = (double) coord.get("lat");
            double lon = (double) coord.get("lon");

            Map<String, Object> sys = (Map<String, Object>) weatherData.get("sys");
            String country = (String) sys.get("country");
            Integer sunriseTime = (Integer) sys.get("sunrise");
            Integer sunsetTime = (Integer) sys.get("sunset");

            // Extract weather description
            List<Map<String, Object>> weatherList = (List<Map<String, Object>>) weatherData.get("weather");
            Map<String, Object> weatherItem = weatherList.get(0); // Assume the first item in the list
            String weatherDescription = (String) weatherItem.get("description");
            String icon = (String) weatherItem.get("icon");

            // Extract the main temperature data
            Map<String, Object> main = (Map<String, Object>) weatherData.get("main");
            double temperature = (double) main.get("temp");
            double feelsLike = (double) main.get("feels_like");
            Integer humidity = (Integer) main.get("humidity");
            Integer pressure = (Integer) main.get("pressure");

            // Extract visibility and wind data
            Integer visibility = (Integer) weatherData.get("visibility");
            Map<String, Object> wind = (Map<String, Object>) weatherData.get("wind");
            double windSpeed = (double) wind.get("speed");

            // Extract cloudiness data
            Integer cloudiness = (Integer) ((Map<String, Object>) weatherData.get("clouds")).get("all");

            // Update location name (optional)
            location.setCityName((String) weatherData.get("name"));

            // Create and save WeatherInfo entity
            WeatherInfo weatherInfo = WeatherInfo.builder()
                    .pinCode(pinCode)
                    .date(forDate)
                    .country(country)
                    .sunriseTime(sunriseTime)
                    .sunsetTime(sunsetTime)
                    .timezoneOffset((Integer) weatherData.get("timezone"))
                    .location(location)
                    .build();

            // Save WeatherInfo to the database
            WeatherInfo savedWeatherInfo = weatherInfoRepository.save(weatherInfo);

            // Create and save WeatherDetail entity (related to WeatherInfo)
            WeatherDetail weatherDetail = WeatherDetail.builder()
                    .weatherInfo(savedWeatherInfo)
                    .main(weatherDescription) // Main weather condition (e.g., "Clear")
                    .description(weatherDescription) // Weather description (e.g., "clear sky")
                    .icon(icon) // Weather icon (optional)
                    .temp(temperature)
                    .feelsLike(feelsLike)
                    .humidity(humidity)
                    .pressure(pressure)
                    .cloudiness(cloudiness)
                    .windSpeed(windSpeed)
                    .visibility(visibility)
                    .build();
            // Save WeatherDetail to the database
            WeatherDetail savedWeatherDetail = weatherDetailRepository.save(weatherDetail);

            // Set the WeatherDetail to the WeatherInfo
            savedWeatherInfo.setWeatherDetails(new ArrayList<>(List.of(savedWeatherDetail)));

            // Return the saved WeatherInfo (this also includes associated WeatherDetail)
            return savedWeatherInfo;

        } catch (HttpClientErrorException e) {
            // Handle errors like 404 or 500 from the API
            throw new RuntimeException("Error fetching weather data: " + e.getMessage(), e);
        }
    }

    private void validateApiResponse(ResponseEntity<Map<String, Object>> response) {
        if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
            throw new RuntimeException("Failed to fetch valid data from OpenWeather API");
        }
    }

    private String extractWeatherDescription(Map<String, Object> weatherDetails) {
        List<Map<String, Object>> weatherList = (List<Map<String, Object>>) weatherDetails.get("weather");
        if (weatherList == null || weatherList.isEmpty()) {
            throw new RuntimeException("Weather description not found in the API response");
        }
        return (String) weatherList.get(0).get("description");
    }

    private double extractTemperature(Map<String, Object> weatherDetails) {
        return extractNumericValue(weatherDetails, "main", "temp");
    }

    private double extractFeelsLikeTemperature(Map<String, Object> weatherDetails) {
        return extractNumericValue(weatherDetails, "main", "feels_like");
    }

    private double extractHumidity(Map<String, Object> weatherDetails) {
        return extractNumericValue(weatherDetails, "main", "humidity");
    }

    private double extractWindSpeed(Map<String, Object> weatherDetails) {
        return extractNumericValue(weatherDetails, "wind", "speed");
    }

    private double extractPressure(Map<String, Object> weatherDetails) {
        return extractNumericValue(weatherDetails, "main", "pressure");
    }

    private double extractCloudiness(Map<String, Object> weatherDetails) {
        return extractNumericValue(weatherDetails, "clouds", "all");
    }

    private double extractVisibility(Map<String, Object> weatherDetails) {
        return extractNumericValue(weatherDetails, "visibility", "visibility");
    }

    /**
     * Helper method to safely extract numeric values (handles Integer and Double).
     */
    private double extractNumericValue(Map<String, Object> weatherDetails, String section, String key) {
        Object sectionData = weatherDetails.get(section);

        // If the section is a Map, proceed to extract from it
        if (sectionData instanceof Map) {
            Map<String, Object> sectionMap = (Map<String, Object>) sectionData;
            if (sectionMap.containsKey(key)) {
                Object value = sectionMap.get(key);
                if (value != null) {
                    if (value instanceof Integer) {
                        return ((Integer) value).doubleValue();
                    } else if (value instanceof Double) {
                        return (Double) value;
                    }
                } else {
                    // Handle null value
                    System.out.println("Value for " + key + " in section " + section + " is null.");
                }
            }
        }
        // If the section is not a Map, check if it's a direct value (e.g., visibility)
        else if (sectionData != null) {
            if (sectionData instanceof Integer) {
                return ((Integer) sectionData).doubleValue();
            } else if (sectionData instanceof Double) {
                return (Double) sectionData;
            }
        }

        // If no value is found or it's null, log it and return a default value (0.0)
        System.out.println("No valid value found for " + key + " in section " + section + ". Returning default value.");
        return 0.0;
    }

}
