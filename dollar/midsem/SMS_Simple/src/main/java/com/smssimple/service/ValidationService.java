package com.smssimple.service;

import com.smssimple.domain.Student;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Service responsible for validating student input.
 * Validation is enforced here (service layer) and also in the UI layer.
 * Returns a list of error messages; empty list means valid.
 */
public class ValidationService {

    private static final Set<Integer> VALID_LEVELS = Set.of(100, 200, 300, 400, 500, 600, 700);

    /**
     * Validates a Student object and returns all validation errors.
     * @param student the student to validate
     * @return list of error messages; empty if valid
     */
    public List<String> validate(Student student) {
        List<String> errors = new ArrayList<>();

        validateStudentId(student.getStudentId(), errors);
        validateFullName(student.getFullName(), errors);
        validateProgramme(student.getProgramme(), errors);
        validateLevel(student.getLevel(), errors);
        validateGpa(student.getGpa(), errors);
        validateEmail(student.getEmail(), errors);
        validatePhone(student.getPhoneNumber(), errors);

        return errors;
    }

    // ---- Individual field validators (also used independently in UI) ----

    public void validateStudentId(String id, List<String> errors) {
        if (id == null || id.isBlank()) {
            errors.add("Student ID is required.");
            return;
        }
        if (id.length() < 4 || id.length() > 20) {
            errors.add("Student ID must be between 4 and 20 characters.");
        }
        if (!id.matches("[A-Za-z0-9]+")) {
            errors.add("Student ID must contain only letters and digits.");
        }
    }

    public void validateFullName(String name, List<String> errors) {
        if (name == null || name.isBlank()) {
            errors.add("Full name is required.");
            return;
        }
        if (name.length() < 2 || name.length() > 60) {
            errors.add("Full name must be between 2 and 60 characters.");
        }
        if (name.matches(".*\\d.*")) {
            errors.add("Full name must not contain digits.");
        }
    }

    public void validateProgramme(String programme, List<String> errors) {
        if (programme == null || programme.isBlank()) {
            errors.add("Programme is required.");
        }
    }

    public void validateLevel(int level, List<String> errors) {
        if (!VALID_LEVELS.contains(level)) {
            errors.add("Level must be one of: 100, 200, 300, 400, 500, 600, 700.");
        }
    }

    public void validateGpa(double gpa, List<String> errors) {
        if (gpa < 0.0 || gpa > 4.0) {
            errors.add("GPA must be between 0.0 and 4.0.");
        }
    }

    public void validateEmail(String email, List<String> errors) {
        if (email == null || email.isBlank()) {
            errors.add("Email is required.");
            return;
        }
        if (!email.contains("@") || !email.contains(".")) {
            errors.add("Email must contain '@' and '.'.");
        }
    }

    public void validatePhone(String phone, List<String> errors) {
        if (phone == null || phone.isBlank()) {
            errors.add("Phone number is required.");
            return;
        }
        if (!phone.matches("\\d+")) {
            errors.add("Phone number must contain digits only.");
        }
        if (phone.length() < 10 || phone.length() > 15) {
            errors.add("Phone number must be between 10 and 15 digits.");
        }
    }

    /**
     * Returns true if all validation passes.
     */
    public boolean isValid(Student student) {
        return validate(student).isEmpty();
    }

    /**
     * Validates a GPA threshold value for use in settings and reports.
     */
    public boolean isValidGpaThreshold(double threshold) {
        return threshold >= 0.0 && threshold <= 4.0;
    }
}
