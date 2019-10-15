package com.pitang.controller;

import com.pitang.controller.errors.ErrorsGeneric;
import com.pitang.event.RecursoCriadoEvent;
import com.pitang.exceptionhandler.CustomExceptionHandler;
import com.pitang.model.User;
import com.pitang.model.dto.UserDTO;
import com.pitang.repository.UserRepository;
import com.pitang.service.UserService;
import com.pitang.service.exception.CarWithLicensePlateDuplicated;
import com.pitang.service.exception.EmailDuplicateException;
import com.pitang.service.exception.LoginDuplicateException;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.util.*;

@RestController
@RequestMapping("/users")
public class UserController extends ErrorsGeneric {

	private final UserRepository userRepository;
	private final UserService userService;
	private final MessageSource messageSource;
	private final ApplicationEventPublisher publisher;
	private final ModelMapper userMapper;

	@Autowired
	public UserController(UserRepository userRepository, UserService userService, MessageSource messageSource,
			ApplicationEventPublisher publisher, ModelMapper userMapper) {
		this.userRepository = userRepository;
		this.userService = userService;
		this.messageSource = messageSource;
		this.publisher = publisher;
		this.userMapper = userMapper;
	}

	@GetMapping
	public Page<UserDTO> getAll(Pageable pageable) {
		return userRepository.findAll(pageable).map(user -> userMapper.map(user, UserDTO.class));
	}

	@GetMapping(params = "firstName")
	public Page<UserDTO> findAllByModel(@RequestParam(required = false, defaultValue = "%") String firstName,
			Pageable pageable) {
		return userRepository.findAllByFirstNameContainingIgnoreCase(firstName, pageable)
				.map(user -> userMapper.map(user, UserDTO.class));
	}

	@PostMapping
	public ResponseEntity<?> save(@Valid @RequestBody User user, BindingResult result, HttpServletResponse response) {

		if (result.hasErrors()) {
			List<CustomExceptionHandler.Error> errors = new ArrayList<>();
			result.getAllErrors()
					.forEach(error -> errors.add(new CustomExceptionHandler.Error(error.getDefaultMessage(), "5")));
			return ResponseEntity.badRequest().body(errors);
		}

		User userSaved = userService.save(user);
		publisher.publishEvent(new RecursoCriadoEvent(this, response, userSaved.getId()));
		return ResponseEntity.status(HttpStatus.CREATED).body(userSaved);
	}

	@PutMapping("/{id}")
	public ResponseEntity<?> update(@PathVariable("id") UUID id, @Valid @RequestBody User user, BindingResult result) {
		if (result.hasErrors()) {
			return ResponseEntity.badRequest().body(new ErrorsGeneric().errorFieldsNull(result));
		}
		return ResponseEntity.ok(userService.update(id, user));
	}
	
	@PutMapping("/{userId}/disassociate-car/{carId}")
	@ResponseStatus(HttpStatus.OK)
	public void disassociateCar(@PathVariable("userId") UUID userId, @PathVariable("carId") UUID carId) {
		userService.disassociateCar(userId, carId);
	}

	@DeleteMapping("/{id}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void delete(@PathVariable("id") UUID id) {
		userService.delete(id);
	}

	@GetMapping("/{id}")
	public ResponseEntity<User> findById(@PathVariable("id") UUID id) {
		Optional<User> user = userRepository.findById(id);
		if (user.isPresent())
			return ResponseEntity.ok().body(user.get());
		else
			return ResponseEntity.notFound().build();
	}

	// ExceptionHandlers

	@ExceptionHandler({ EmailDuplicateException.class })
	public ResponseEntity<Object> handleEmailDuplicateException() {
		String customMessageUser = messageSource.getMessage("validation.user-email-duplicate", null,
				LocaleContextHolder.getLocale());
		List<CustomExceptionHandler.Error> errors = Collections
				.singletonList(new CustomExceptionHandler.Error(customMessageUser, "2"));
		return ResponseEntity.badRequest().body(errors);
	}

	@ExceptionHandler({ LoginDuplicateException.class })
	public ResponseEntity<Object> handleLoginDuplicateException() {
		String customMessageUser = messageSource.getMessage("validation.user-login-duplicate", null,
				LocaleContextHolder.getLocale());
		List<CustomExceptionHandler.Error> errors = Collections
				.singletonList(new CustomExceptionHandler.Error(customMessageUser, "3"));
		return ResponseEntity.badRequest().body(errors);
	}

	@ExceptionHandler({ CarWithLicensePlateDuplicated.class })
	public ResponseEntity<Object> handleCarWithLicensePlateDuplicated() {
		String customMessageUser = messageSource.getMessage("validation.car-license-plate-in-use", null,
				LocaleContextHolder.getLocale());
		List<CustomExceptionHandler.Error> errors = Collections
				.singletonList(new CustomExceptionHandler.Error(customMessageUser, "3"));
		return ResponseEntity.badRequest().body(errors);
	}

}
