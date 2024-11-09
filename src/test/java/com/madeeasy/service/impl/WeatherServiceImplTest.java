package com.madeeasy.service.impl;

import com.madeeasy.entity.PinCodeLocation;
import com.madeeasy.entity.WeatherDetail;
import com.madeeasy.entity.WeatherInfo;
import com.madeeasy.repository.PinCodeLocationRepository;
import com.madeeasy.repository.WeatherDetailRepository;
import com.madeeasy.repository.WeatherInfoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
class WeatherServiceImplTest {

    @Mock
    private PinCodeLocationRepository pinCodeLocationRepository;

    @Mock
    private WeatherInfoRepository weatherInfoRepository;

    @Mock
    private WeatherDetailRepository weatherDetailRepository;

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private WeatherServiceImpl weatherServiceImpl;

    @BeforeEach
    void setUp() {
    }

    @Test
    void testGetWeatherInfo_CachedData() {
        String pinCode = "123456";
        LocalDate forDate = LocalDate.now();
        WeatherInfo cachedWeatherInfo = new WeatherInfo();
        when(weatherInfoRepository.findByPinCodeAndDate(pinCode, forDate)).thenReturn(Optional.of(cachedWeatherInfo));

        WeatherInfo result = weatherServiceImpl.getWeatherInfo(pinCode, forDate);

        assertEquals(cachedWeatherInfo, result);
        verify(weatherInfoRepository, never()).save(any(WeatherInfo.class));
    }



    @Test
    void testGetWeatherInfo_NewData() {
        // Arrange
        String pinCode = "123456";
        LocalDate forDate = LocalDate.now();

        PinCodeLocation location = new PinCodeLocation();
        location.setPinCode(pinCode);
        location.setLatitude(12.34);
        location.setLongitude(56.78);

        WeatherInfo expectedWeatherInfo = WeatherInfo.builder()
                .pinCode(pinCode)
                .date(forDate)
                .country("IN")
                .sunriseTime(12345)
                .sunsetTime(67890)
                .timezoneOffset(19800)
                .location(location)
                .build();

        WeatherDetail expectedWeatherDetail = WeatherDetail.builder()
                .main("clear sky")
                .description("clear sky")
                .icon("01d")
                .temp(25.0)
                .feelsLike(23.0)
                .humidity(60)
                .pressure(1012)
                .cloudiness(20)
                .windSpeed(5.5)
                .visibility(10000)
                .weatherInfo(expectedWeatherInfo)
                .build();

        expectedWeatherInfo.setWeatherDetails(new ArrayList<>(List.of(expectedWeatherDetail)));

        // Mock response data structure to match the expected response from the weather API
        Map<String, Object> responseBody = new HashMap<>();
        responseBody.put("coord", Map.of("lat", 12.34, "lon", 56.78));
        responseBody.put("sys", Map.of("country", "IN", "sunrise", 12345, "sunset", 67890));
        responseBody.put("weather", List.of(Map.of("description", "clear sky", "icon", "01d")));
        responseBody.put("main", Map.of("temp", 25.0, "feels_like", 23.0, "humidity", 60, "pressure", 1012));
        responseBody.put("visibility", 10000);
        responseBody.put("wind", Map.of("speed", 5.5));
        responseBody.put("clouds", Map.of("all", 20));
        responseBody.put("name", "CityName");
        responseBody.put("timezone", 19800);

        ResponseEntity<Map<String, Object>> response = new ResponseEntity<>(responseBody, HttpStatus.OK);

        // Mock repository and RestTemplate responses
        when(weatherInfoRepository.findByPinCodeAndDate(pinCode, forDate)).thenReturn(Optional.empty());
        when(pinCodeLocationRepository.findByPinCode(pinCode)).thenReturn(Optional.of(location));
        when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), isNull(), any(ParameterizedTypeReference.class)))
                .thenReturn(response);
        when(weatherInfoRepository.save(any(WeatherInfo.class))).thenReturn(expectedWeatherInfo);
        when(weatherDetailRepository.save(any(WeatherDetail.class))).thenReturn(expectedWeatherDetail);

        // Act
        WeatherInfo actualWeatherInfo = weatherServiceImpl.getWeatherInfo(pinCode, forDate);

        // Assert
        assertEquals(expectedWeatherInfo, actualWeatherInfo);
        assertNotNull(actualWeatherInfo.getWeatherDetails());
        assertEquals(1, actualWeatherInfo.getWeatherDetails().size());

        WeatherDetail actualWeatherDetail = actualWeatherInfo.getWeatherDetails().get(0);
        assertEquals("clear sky", actualWeatherDetail.getMain());
        assertEquals("clear sky", actualWeatherDetail.getDescription());
        assertEquals(25.0, actualWeatherDetail.getTemp());
        assertEquals(23.0, actualWeatherDetail.getFeelsLike());
        assertEquals(60, actualWeatherDetail.getHumidity());
        assertEquals(1012, actualWeatherDetail.getPressure());
        assertEquals(5.5, actualWeatherDetail.getWindSpeed());
        assertEquals(10000, actualWeatherDetail.getVisibility());
        assertEquals(20, actualWeatherDetail.getCloudiness());

        verify(weatherInfoRepository).findByPinCodeAndDate(pinCode, forDate);
        verify(pinCodeLocationRepository).findByPinCode(pinCode);
        verify(weatherInfoRepository, times(2)).save(any(WeatherInfo.class));
        verify(weatherDetailRepository).save(any(WeatherDetail.class));
    }



    @Test
    void testFetchAndSaveLocation_Success() {
        String pinCode = "123456";
        Map<String, Object> responseBody = new HashMap<>();
        responseBody.put("lat", 12.34);
        responseBody.put("lon", 56.78);

        ResponseEntity<Map<String, Object>> response = new ResponseEntity<>(responseBody, HttpStatus.OK);
        when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), isNull(),
                any(ParameterizedTypeReference.class))).thenReturn(response);
        when(pinCodeLocationRepository.save(any(PinCodeLocation.class))).thenAnswer(i -> i.getArguments()[0]);

        PinCodeLocation result = weatherServiceImpl.fetchAndSaveLocation(pinCode);

        assertEquals(pinCode, result.getPinCode());
        assertEquals(12.34, result.getLatitude());
        assertEquals(56.78, result.getLongitude());
        verify(pinCodeLocationRepository).save(any(PinCodeLocation.class));
    }


    @Test
    public void testFetchWeatherData_Success() {
        // Prepare test data
        PinCodeLocation location = new PinCodeLocation();
        location.setLatitude(12.34);
        location.setLongitude(56.78);
        String pinCode = "123456";
        LocalDate forDate = LocalDate.now();

        // Mock the response from external weather API
        Map<String, Object> responseBody = new HashMap<>();
        responseBody.put("coord", Map.of("lat", 12.34, "lon", 56.78));
        responseBody.put("sys", Map.of("country", "IN", "sunrise", 12345, "sunset", 67890));
        responseBody.put("weather", List.of(Map.of("description", "clear sky", "icon", "01d")));
        responseBody.put("main", Map.of("temp", 25.0, "feels_like", 23.0, "humidity", 60, "pressure", 1012));
        responseBody.put("visibility", 10000);
        responseBody.put("wind", Map.of("speed", 5.5));
        responseBody.put("clouds", Map.of("all", 20));
        responseBody.put("name", "CityName");
        responseBody.put("timezone", 19800);

        ResponseEntity<Map<String, Object>> response = new ResponseEntity<>(responseBody, HttpStatus.OK);
        when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), isNull(), any(ParameterizedTypeReference.class))).thenReturn(response);

        // Mock the save calls to repositories
        WeatherInfo savedWeatherInfo = WeatherInfo.builder()
                .pinCode(pinCode)
                .date(forDate)
                .country("IN")
                .sunriseTime(12345)
                .sunsetTime(67890)
                .timezoneOffset(19800)
                .location(location)
                .build();

        WeatherDetail savedWeatherDetail = WeatherDetail.builder()
                .main("clear sky")
                .description("clear sky")
                .icon("01d")
                .temp(25.0)
                .feelsLike(23.0)
                .humidity(60)
                .pressure(1012)
                .cloudiness(20)
                .windSpeed(5.5)
                .visibility(10000)
                .build();

        when(weatherInfoRepository.save(any(WeatherInfo.class))).thenReturn(savedWeatherInfo);
        when(weatherDetailRepository.save(any(WeatherDetail.class))).thenReturn(savedWeatherDetail);

        // Call the method under test
        WeatherInfo result = weatherServiceImpl.fetchWeatherData(location, pinCode, forDate);

        // Validate WeatherInfo properties
        assertEquals("IN", result.getCountry());
        assertEquals(pinCode, result.getPinCode());
        assertEquals(12345, result.getSunriseTime());
        assertEquals(67890, result.getSunsetTime());
        assertEquals(19800, result.getTimezoneOffset());

        // Validate associated WeatherDetail properties
        WeatherDetail detail = result.getWeatherDetails().get(0);
        assertEquals("clear sky", detail.getDescription());
        assertEquals(25.0, detail.getTemp());
        assertEquals("01d", detail.getIcon());

        // Verify repository interactions
        verify(weatherInfoRepository).save(any(WeatherInfo.class));
        verify(weatherDetailRepository).save(any(WeatherDetail.class));
    }

}
