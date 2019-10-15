package com.pitang.service;

import com.pitang.model.Car;
import com.pitang.model.User;
import com.pitang.repository.CarRepository;
import com.pitang.repository.UserRepository;
import com.pitang.service.exception.CarWithLicensePlateDuplicated;
import com.pitang.service.exception.EmailDuplicateException;
import com.pitang.service.exception.LoginDuplicateException;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class UserService {

	private final UserRepository userRepository;
	private final CarRepository carRepository;

	@Autowired
	public UserService(UserRepository userRepository, CarRepository carRepository) {
		this.userRepository = userRepository;
		this.carRepository = carRepository;
	}

	@Transactional
	public User save(User user) {
		validateEmailDuplicate(user);
		validateLoginDuplicate(user);
		List<Car> cars = user.getCars();
		user.setCars(new ArrayList<>());
		user = userRepository.save(user);
		saveListCars(user, cars);
		return user;
	}

	private void validateEmailDuplicate(User user) {

		// Validação para novo usuário
		if (user.getId() == null) {
			if (userRepository.findByEmail(user.getEmail()).isPresent())
				throw new EmailDuplicateException();
		}

		// Validação para usuário já cadastrado
		if (user.getId() != null && user.getEmail() != null) {
			Optional<User> userSaved = userRepository.findByEmail(user.getEmail());
			if (userSaved.isPresent()) {
				if (userSaved.get().getId() != user.getId()) {
					throw new EmailDuplicateException();
				}

			}

		}

	}

	private void validateLoginDuplicate(User user) {

		// Validação para novo usuário
		if (user.getId() == null) {
			if (userRepository.findByLogin(user.getLogin()).isPresent())
				throw new LoginDuplicateException();
		}

		// Validação para usuário já cadastrado
		if (user.getId() != null && user.getLogin() != null) {
			Optional<User> userSaved = userRepository.findByLogin(user.getLogin());

			if (userSaved.isPresent()) {
				if (userSaved.get().getId() != user.getId()) {
					throw new LoginDuplicateException();
				}
			}
		}

	}

	private void saveListCars(User user, List<Car> cars) {
		if (cars != null) {
			for (Car car : cars) {
				if (car.getId() == null) {
					validatePlate(car);
					car.setUser(user);
					carRepository.saveAndFlush(car);
				}
			}
		}
	}

	@Transactional
	public User update(UUID id, User user) {
		Optional<User> userSaved = findById(id);
		if (userSaved.isPresent()) {
			BeanUtils.copyProperties(user, userSaved.get(), "id");
			return this.save(userSaved.get());
		} else
			return null;
	}

	/*
	 ****
	 * Verificação para saber se o id corresponde a um carro no banco de dados. Se
	 * não encontrou, então, devolve um notFound para o front.
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

	public void disassociateCar(UUID userId, UUID carId) {
		Optional<Car> car = carRepository.findByUserIdAndId(userId, carId);

		if (car.isPresent()) {
			car.get().setUser(null);
			carRepository.save(car.get());
		}
	}

	private void validatePlate(Car car) {
		Car carWithLicensePlateDuplicated = carRepository.findByLicensePlate(car.getLicensePlate());

		/*
		 ****
		 * Se a placa estiver em uso, então é lançada uma exceção que será tratada por
		 * CustomExceptionHandler
		 */
		if (carWithLicensePlateDuplicated != null && carWithLicensePlateDuplicated.getId() != car.getId()) {
			throw new CarWithLicensePlateDuplicated();
		}
	}

}
