package com.madeeasy.repository;

import com.madeeasy.entity.WeatherInfo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.Optional;

public interface WeatherInfoRepository extends JpaRepository<WeatherInfo, Long> {
    Optional<WeatherInfo> findByPinCodeAndDate(String pinCode, LocalDate forDate);
}
