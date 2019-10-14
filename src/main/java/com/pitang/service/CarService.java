package com.pitang.service;

import com.pitang.model.Car;
import com.pitang.repository.CarRepository;
import com.pitang.service.exception.CarWithLicensePlateDuplicated;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Service
public class CarService {

    private final CarRepository carRepository;

    @Autowired
    public CarService(CarRepository carRepository) {
        this.carRepository = carRepository;
    }

    @Transactional
    public Car save(Car car) {
        /*
         * Varificação para placa em uso!
         */
        validatePlate(car);

        car = carRepository.save(car);
        return car;
    }

    @Transactional
    public Car update(UUID id, Car car) {
        Optional<Car> carSaved = findById(id);
        if (carSaved.isPresent()) {
            BeanUtils.copyProperties(car, carSaved.get(), "id");
            return this.save(carSaved.get());
        } else return null;
    }

    /*
     ****
     * Verificação para saber se o id corresponde a um carro no banco de dados.
     * Se não encontrou, então, devolve um notFound para o front.
     */
    private Optional<Car> findById(UUID id) {
        Optional<Car> carSaved = carRepository.findById(id);

        if (!carSaved.isPresent()) {
            throw new EmptyResultDataAccessException(1);
        }

        return carSaved;
    }

    private void validatePlate(Car car) {

        if (car.isNewCar()) {
            Car carWithLicensePlateDuplicated = carRepository.findByLicensePlate(car.getLicensePlate());

            /*
             ****
             * Se a placa estiver em uso, então é lançada uma exceção que será tratada por CustomExceptionHandler
             */
            if (carWithLicensePlateDuplicated != null) {
                throw new CarWithLicensePlateDuplicated();
            }
        }
    }
}
