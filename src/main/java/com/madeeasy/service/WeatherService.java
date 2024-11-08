package com.madeeasy.service;

import com.madeeasy.entity.WeatherInfo;

import java.time.LocalDate;

public interface WeatherService {
    WeatherInfo getWeatherInfo(String pinCode, LocalDate forDate);
}
