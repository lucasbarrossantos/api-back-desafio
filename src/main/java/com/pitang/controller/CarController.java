package com.pitang.controller;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.pitang.controller.errors.ErrorsGeneric;
import com.pitang.event.RecursoCriadoEvent;
import com.pitang.exceptionhandler.CustomExceptionHandler;
import com.pitang.model.Car;
import com.pitang.repository.CarRepository;
import com.pitang.service.CarService;
import com.pitang.service.exception.CarWithLicensePlateDuplicated;

@RestController
@RequestMapping("/cars")
public class CarController {

	private final CarRepository carRepository;
	private final CarService carService;
	private final MessageSource messageSource;
	private final ApplicationEventPublisher publisher;

	@Autowired
	public CarController(CarRepository carRepository, CarService carService, MessageSource messageSource,
			ApplicationEventPublisher publisher) {
		this.carRepository = carRepository;
		this.carService = carService;
		this.messageSource = messageSource;
		this.publisher = publisher;
	}

	@GetMapping
	public Page<Car> getAll(Pageable pageable) {
		return carRepository.findAll(pageable);
	}

	@GetMapping(params = "model")
	public Page<Car> findAllByModel(@RequestParam(required = false, defaultValue = "%") String model,
			Pageable pageable) {
		return carRepository.findAllByModelContainingIgnoreCase(model, pageable);
	}

	@PostMapping
	public ResponseEntity<?> save(@Valid @RequestBody Car car, BindingResult result, HttpServletResponse response) {

		if (result.hasErrors()) {
			List<CustomExceptionHandler.Error> errors = new ArrayList<>();
			result.getAllErrors()
					.forEach(error -> errors.add(new CustomExceptionHandler.Error(error.getDefaultMessage(), "5")));
			return ResponseEntity.badRequest().body(errors);
		}

		Car carSaved = carService.save(car);
		publisher.publishEvent(new RecursoCriadoEvent(this, response, carSaved.getId()));
		return ResponseEntity.status(HttpStatus.CREATED).body(carSaved);
	}

	@PutMapping("/{id}")
	public ResponseEntity<?> update(@PathVariable("id") UUID id, @Valid @RequestBody Car car, BindingResult result) {
		if (result.hasErrors()) {
			return ResponseEntity.badRequest().body(new ErrorsGeneric().errorFieldsNull(result));
		}
		return ResponseEntity.ok(carService.update(id, car));
	}

	@DeleteMapping("/{id}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void delete(@PathVariable("id") UUID id) {
		carRepository.deleteById(id);
	}

	@GetMapping("/{id}")
	public ResponseEntity<Car> findById(@PathVariable("id") UUID id) {
		Optional<Car> car = carRepository.findById(id);
		if (car.isPresent()) {
			return ResponseEntity.ok().body(car.get());
		} else
			return ResponseEntity.notFound().build();
	}

	// ExceptionHandlers

	@ExceptionHandler({ CarWithLicensePlateDuplicated.class })
	public ResponseEntity<Object> handleCarWithLicensePlateDuplicated() {
		String customMessageUser = messageSource.getMessage("validation.car-license-plate-in-use", null,
				LocaleContextHolder.getLocale());
		List<CustomExceptionHandler.Error> errors = Collections
				.singletonList(new CustomExceptionHandler.Error(customMessageUser, "3"));
		return ResponseEntity.badRequest().body(errors);
	}

}
