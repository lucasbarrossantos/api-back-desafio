package com.pitang.repository;

import com.pitang.model.Car;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface CarRepository extends JpaRepository<Car, UUID> {

    Car findByLicensePlate(String licensePlate);

    Page<Car> findAllByModelContainingIgnoreCase(String model, Pageable pageable);
    
    Optional<Car> findByUserIdAndId(UUID userId, UUID carId);
}
