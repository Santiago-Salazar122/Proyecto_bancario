package com.bank.infrastructure.api.v1;

import com.bank.domain.enums.UserRole;
import com.bank.domain.enums.UserStatus;
import com.bank.domain.model.user.User;
import com.bank.domain.ports.UserRepositoryPort;
import com.bank.domain.service.RegisterUserService;
import com.bank.domain.valueobject.Address;
import com.bank.domain.valueobject.Email;
import com.bank.domain.valueobject.PhoneNumber;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;
import java.util.List;

/**
 * Controller REST para gestión de usuarios.
 * URL base: /api/v1/users
 */
@RestController
@RequestMapping("/api/v1/users")
public class UserController {

    private final RegisterUserService registerUserService;
    private final UserRepositoryPort userRepository;

    public UserController(RegisterUserService registerUserService,
                          UserRepositoryPort userRepository) {
        this.registerUserService = registerUserService;
        this.userRepository = userRepository;
    }

    /** POST /api/v1/users — Registra un nuevo usuario (201 Created) */
    @PostMapping
    public ResponseEntity<User> registerUser(@Valid @RequestBody RegisterUserRequest request) {
        User user = new User(request.relatedId, request.fullName, request.identificationId,
            new Email(request.email), new PhoneNumber(request.phoneNumber),
            request.dateOfBirth, new Address(request.address),
            request.role, request.username, request.password);
        return ResponseEntity.status(HttpStatus.CREATED).body(registerUserService.execute(user));
    }

    /** GET /api/v1/users/{id} — Busca por ID interno */
    @GetMapping("/{id}")
    public ResponseEntity<User> findById(@PathVariable Long id) {
        return userRepository.findById(id).map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    /** GET /api/v1/users/identification/{id} — Busca por CC/NIT */
    @GetMapping("/identification/{identificationId}")
    public ResponseEntity<User> findByIdentificationId(@PathVariable String identificationId) {
        return userRepository.findByIdentificationId(identificationId)
            .map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    /** GET /api/v1/users  o  GET /api/v1/users?role=INTERNAL_ANALYST */
    @GetMapping
    public ResponseEntity<List<User>> listAll(@RequestParam(required = false) UserRole role) {
        if (role != null) return ResponseEntity.ok(userRepository.findByRole(role));
        return ResponseEntity.ok(userRepository.findAll());
    }

    /** PATCH /api/v1/users/{id}/status?status=BLOCKED — Cambia estado */
    @PatchMapping("/{id}/status")
    public ResponseEntity<User> updateStatus(@PathVariable Long id, @RequestParam UserStatus status) {
        User user = userRepository.findById(id)
            .orElseThrow(() -> new IllegalStateException("User not found: " + id));
        user.changeStatus(status);
        return ResponseEntity.ok(userRepository.save(user));
    }

    static class RegisterUserRequest {
        @NotBlank(message = "Full name is required") @Size(max = 100) String fullName;
        @NotBlank(message = "Identification ID is required") @Size(max = 20) String identificationId;
        @NotBlank @Email(message = "Email must contain @ and a valid domain") String email;
        @NotBlank @Pattern(regexp = "\\d{7,15}", message = "Phone must have 7 to 15 digits") String phoneNumber;
        LocalDate dateOfBirth;
        @NotBlank(message = "Address is required") String address;
        @NotNull(message = "Role is required") UserRole role;
        @NotBlank(message = "Username is required") String username;
        @NotBlank(message = "Password is required") String password;
        String relatedId;
    }
}
