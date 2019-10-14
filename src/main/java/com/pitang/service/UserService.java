package com.pitang.service;

import com.pitang.model.Car;
import com.pitang.model.User;
import com.pitang.repository.UserRepository;
import com.pitang.service.exception.EmailDuplicateException;
import com.pitang.service.exception.LoginDuplicateException;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final CarService carService;

    @Autowired
    public UserService(UserRepository userRepository,
                       CarService carService) {
        this.userRepository = userRepository;
        this.carService = carService;
    }

    @Transactional
    public User save(User user) {
        validateEmailDuplicate(user);
        validateLoginDuplicate(user);
        List<Car> cars = user.getCars();
        user = userRepository.save(user);
        saveListCars(user, cars);
        return user;
    }

    private void validateEmailDuplicate(User user) {
        if (userRepository.findByEmail(user.getEmail()).isPresent())
            throw new EmailDuplicateException();
    }

    private void validateLoginDuplicate(User user) {
        if (userRepository.findByLogin(user.getLogin()).isPresent())
            throw new LoginDuplicateException();
    }

    private void saveListCars(User user, List<Car> cars) {
        if (cars != null)
            cars.forEach(car -> {
                car.setUser(user);
                carService.save(car);
            });
    }

    @Transactional
    public User update(UUID id, User user) {
        Optional<User> userSaved = findById(id);
        if (userSaved.isPresent()) {
            BeanUtils.copyProperties(user, userSaved.get(), "id", "cars");
            return this.save(userSaved.get());
        } else return null;
    }

    /*
     ****
     * Verificação para saber se o id corresponde a um carro no banco de dados.
     * Se não encontrou, então, devolve um notFound para o front.
     */
    private Optional<User> findById(UUID id) {
        Optional<User> carSaved = userRepository.findById(id);

        if (!carSaved.isPresent()) {
            throw new EmptyResultDataAccessException(1);
        }

        return carSaved;
    }

    public void delete(UUID id) {
        Optional<User> userSaved = findById(id);
        userSaved.ifPresent(user -> user.getCars().forEach(car -> car.setUser(null)));
        userRepository.deleteById(id);
    }
}
