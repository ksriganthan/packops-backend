package ch.packops.packopsbackend.service;

import ch.packops.packopsbackend.dto.*;
import ch.packops.packopsbackend.repository.ProductConfigurationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author Kapischan
 */

@Service

public class ValidationService {

    @Autowired
    private ProductConfigurationRepository productConfigurationRepository;


    public ValidationService() {

    }

    // Validation ConfigurationDto
    public void validateConfiguration(ConfigurationDto dto) {
        if (dto == null) {
            throw new IllegalArgumentException("Configuration cannot be null");
        }
        if (dto.getTargetWeight() == null || dto.getTargetWeight() < 50 || dto.getTargetWeight() > 500) {
            throw new IllegalArgumentException("TargetWeight must be between 50 and 500");
        }
        if (dto.getTolerance() == null || dto.getTolerance() < 0) {
            throw new IllegalArgumentException("Tolerance must be positive");
        }
        if (dto.getMaxUnits() == null || dto.getMaxUnits() <= 0) {
            throw new IllegalArgumentException("MaxUnits must be greater than 0");
        }
        if (dto.getMaxIterationsForReject() == null || dto.getMaxIterationsForReject() <= 0) {
            throw new IllegalArgumentException("MaxIterations must be greater than 0");
        }
    }

    // Validation ProductConfigurationCreateDto
    public void validateProduct(ProductConfigurationCreateDto dto) {
        if (dto == null) {
            throw new IllegalArgumentException("Product cannot be null");
        }
        if (dto.getTranslations() == null || dto.getTranslations().isEmpty()) {
            throw new IllegalArgumentException("Product translations cannot be empty (DE, FR, EN)");
        }
        if (dto.getTargetWeight() == null || dto.getTargetWeight() < 50 || dto.getTargetWeight() > 500) {
            throw new IllegalArgumentException("TargetWeight must be between 50 and 500");
        }
        if (dto.getTolerance() == null || dto.getTolerance() < 0) {
            throw new IllegalArgumentException("Tolerance must be positive");
        }
    }

    // Validation ProductConfigurationUpdateDto
    public void validateProductUpdate(ProductConfigurationUpdateDto dto) {
        if (dto == null) {
            throw new IllegalArgumentException("Product cannot be null");
        }
        // Partial Update: Nur vorhandene (nicht-null) Felder validieren
        if (dto.getProductName() != null && dto.getProductName().isEmpty()) {
            throw new IllegalArgumentException("Product name cannot be empty");
        }
        if (dto.getTargetWeight() != null && (dto.getTargetWeight() < 50 || dto.getTargetWeight() > 500)) {
            throw new IllegalArgumentException("TargetWeight must be between 50 and 500");
        }
        if (dto.getTolerance() != null && dto.getTolerance() < 0) {
            throw new IllegalArgumentException("Tolerance must be positive");
        }
    }

    // Validation UserCreateDto
    public void validateUser(UserCreateDto dto) {
        if (dto == null) {
            throw new IllegalArgumentException("User cannot be null");
        }
        if (dto.getUsername() == null || dto.getUsername().isEmpty()) {
            throw new IllegalArgumentException("Username cannot be empty");
        }
        if (dto.getEmail() == null || dto.getEmail().isEmpty()) {
            throw new IllegalArgumentException("Email cannot be empty");
        }
        if (!dto.getEmail().contains("@")) {
            throw new IllegalArgumentException("Email is not valid");
        }
        if (dto.getPassword() == null || dto.getPassword().length() < 6) {
            throw new IllegalArgumentException("Password must be at least 6 characters");
        }
        if (dto.getRole() == null || dto.getRole().isEmpty()) {
            throw new IllegalArgumentException("Role cannot be empty");
        }
        String roleUpper = dto.getRole().toUpperCase();
        if (!roleUpper.equals("ADMIN") && !roleUpper.equals("OPERATOR") && !roleUpper.equals("VIEWER")) {
            throw new IllegalArgumentException("Role must be one of: admin, operator, viewer");
        }
    }

    // Validation UserUpdateDto
    public void validateUserUpdate(UserUpdateDto dto) {
        if (dto == null) {
            throw new IllegalArgumentException("User cannot be null");
        }
        // Partial Update: Nur vorhandene (nicht-null) Felder validieren
        if (dto.getEmail() != null && !dto.getEmail().isEmpty() && !dto.getEmail().contains("@")) {
            throw new IllegalArgumentException("Email is not valid");
        }
        if (dto.getPassword() != null && dto.getPassword().length() < 6) {
            throw new IllegalArgumentException("Password must be at least 6 characters");
        }
        if (dto.getOldPassword() != null && dto.getPassword() == null) {
            throw new IllegalArgumentException("New password cannot be empty if old password is provided");
        }
    }

    // Validation ProcessStartDto
    public void validateProcess(ProcessStartDto dto) {
        if (dto == null) {
            throw new IllegalArgumentException("Process cannot be null");
        }

        // Todo: Noch mit der Gruppe prüfen
        // wenn kein Produkt ausgewählt wird, so wird momentan null im Backend gespeichert, eventuell noch so anpassen?
        // Im Zusammenhang wenn jemand einen Prozess ohne ein Produkt startet (kann verschiedene gründe haben)
        if (dto.getProductConfigurationId() == null) {
            // throw new IllegalArgumentException("ProductConfigurationId cannot be null");
        }

        // Validiere targetWeight (falls vorhanden)
        if (dto.getTargetWeight() != null && (dto.getTargetWeight() < 50 || dto.getTargetWeight() > 500)) {
            throw new IllegalArgumentException("TargetWeight must be between 50 and 500");
        }
        // Validiere tolerance (falls vorhanden)
        if (dto.getTolerance() != null && dto.getTolerance() < 0) {
            throw new IllegalArgumentException("Tolerance must be positive");
        }
        // Validiere maxUnits (falls vorhanden)
        if (dto.getMaxUnits() != null && dto.getMaxUnits() < 0) {
            throw new IllegalArgumentException("MaxUnits cannot be negative");
        }
        // Validiere maxIterationsForReject (falls vorhanden)
        if (dto.getMaxIterationsForReject() != null && dto.getMaxIterationsForReject() <= 0) {
            throw new IllegalArgumentException("MaxIterationsForReject must be greater than 0");
        }
        if (dto.getProductConfigurationId() != null && productConfigurationRepository.findById(dto.getProductConfigurationId()).isEmpty()) {
            throw new IllegalArgumentException(String.format("Product with ID %s does not exist", dto.getProductConfigurationId()));
        } else if (dto.getProductConfigurationId() != null && !productConfigurationRepository.findById(dto.getProductConfigurationId()).get().getActive()) {
            throw new IllegalArgumentException((String.format("Product with ID %s is deactivated", dto.getProductConfigurationId())));
        }
    }
}