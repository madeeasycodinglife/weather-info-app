package com.madeeasy.repository;

import com.madeeasy.entity.PinCodeLocation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PinCodeLocationRepository extends JpaRepository<PinCodeLocation, Long> {
    Optional<PinCodeLocation> findByPinCode(String pinCode);
}
