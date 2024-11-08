package com.madeeasy.repository;

import com.madeeasy.entity.WeatherDetail;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WeatherDetailRepository extends JpaRepository<WeatherDetail, Long> {
}
