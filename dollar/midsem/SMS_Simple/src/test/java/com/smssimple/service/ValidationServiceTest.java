package com.smssimple.service;

import com.smssimple.domain.Student;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import java.util.ArrayList;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for ValidationService.
 * Tests all validation rules defined in the assignment specification.
 */
class ValidationServiceTest {

    private ValidationService validationService;

    @BeforeEach
    void setUp() {
        validationService = new ValidationService();
    }

    // ========== Student ID Tests ==========

    @Test
    @DisplayName("Valid student ID passes validation")
    void testValidStudentId() {
        List<String> errors = new ArrayList<>();
        validationService.validateStudentId("STU1234", errors);
        assertTrue(errors.isEmpty(), "Expected no errors for valid ID");
    }

    @Test
    @DisplayName("Student ID that is too short fails validation")
    void testStudentIdTooShort() {
        List<String> errors = new ArrayList<>();
        validationService.validateStudentId("AB", errors);
        assertFalse(errors.isEmpty(), "Expected error for ID that is too short");
    }

    @Test
    @DisplayName("Student ID that is too long fails validation")
    void testStudentIdTooLong() {
        List<String> errors = new ArrayList<>();
        validationService.validateStudentId("ABCDEFGHIJKLMNOPQRSTU", errors); // 21 chars
        assertFalse(errors.isEmpty(), "Expected error for ID that is too long");
    }

    @Test
    @DisplayName("Student ID with special characters fails validation")
    void testStudentIdSpecialChars() {
        List<String> errors = new ArrayList<>();
        validationService.validateStudentId("STU-123!", errors);
        assertFalse(errors.isEmpty(), "Expected error for ID with special characters");
    }

    @Test
    @DisplayName("Blank student ID fails validation")
    void testBlankStudentId() {
        List<String> errors = new ArrayList<>();
        validationService.validateStudentId("", errors);
        assertFalse(errors.isEmpty(), "Expected error for blank ID");
    }

    // ========== Full Name Tests ==========

    @Test
    @DisplayName("Valid full name passes validation")
    void testValidFullName() {
        List<String> errors = new ArrayList<>();
        validationService.validateFullName("John Doe", errors);
        assertTrue(errors.isEmpty(), "Expected no errors for valid name");
    }

    @Test
    @DisplayName("Full name with digits fails validation")
    void testFullNameWithDigits() {
        List<String> errors = new ArrayList<>();
        validationService.validateFullName("John Doe2", errors);
        assertFalse(errors.isEmpty(), "Expected error for name containing digits");
    }

    @Test
    @DisplayName("Full name that is too short fails validation")
    void testFullNameTooShort() {
        List<String> errors = new ArrayList<>();
        validationService.validateFullName("A", errors);
        assertFalse(errors.isEmpty(), "Expected error for name that is too short");
    }

    // ========== Level Tests ==========

    @Test
    @DisplayName("Valid level 300 passes validation")
    void testValidLevel() {
        List<String> errors = new ArrayList<>();
        validationService.validateLevel(300, errors);
        assertTrue(errors.isEmpty(), "Expected no errors for valid level 300");
    }

    @Test
    @DisplayName("Invalid level 150 fails validation")
    void testInvalidLevel() {
        List<String> errors = new ArrayList<>();
        validationService.validateLevel(150, errors);
        assertFalse(errors.isEmpty(), "Expected error for invalid level 150");
    }

    // ========== GPA Tests ==========

    @Test
    @DisplayName("Valid GPA of 3.5 passes validation")
    void testValidGpa() {
        List<String> errors = new ArrayList<>();
        validationService.validateGpa(3.5, errors);
        assertTrue(errors.isEmpty(), "Expected no errors for GPA 3.5");
    }

    @Test
    @DisplayName("GPA above 4.0 fails validation")
    void testGpaAboveMax() {
        List<String> errors = new ArrayList<>();
        validationService.validateGpa(4.5, errors);
        assertFalse(errors.isEmpty(), "Expected error for GPA above 4.0");
    }

    @Test
    @DisplayName("Negative GPA fails validation")
    void testNegativeGpa() {
        List<String> errors = new ArrayList<>();
        validationService.validateGpa(-0.1, errors);
        assertFalse(errors.isEmpty(), "Expected error for negative GPA");
    }

    // ========== Email Tests ==========

    @Test
    @DisplayName("Valid email passes validation")
    void testValidEmail() {
        List<String> errors = new ArrayList<>();
        validationService.validateEmail("student@university.edu", errors);
        assertTrue(errors.isEmpty(), "Expected no errors for valid email");
    }

    @Test
    @DisplayName("Email without @ fails validation")
    void testEmailMissingAt() {
        List<String> errors = new ArrayList<>();
        validationService.validateEmail("studentuniversity.edu", errors);
        assertFalse(errors.isEmpty(), "Expected error for email missing @");
    }

    @Test
    @DisplayName("Email without dot fails validation")
    void testEmailMissingDot() {
        List<String> errors = new ArrayList<>();
        validationService.validateEmail("student@universityedu", errors);
        assertFalse(errors.isEmpty(), "Expected error for email missing dot");
    }

    // ========== Phone Tests ==========

    @Test
    @DisplayName("Valid 10-digit phone passes validation")
    void testValidPhone() {
        List<String> errors = new ArrayList<>();
        validationService.validatePhone("0241234567", errors);
        assertTrue(errors.isEmpty(), "Expected no errors for valid phone");
    }

    @Test
    @DisplayName("Phone with letters fails validation")
    void testPhoneWithLetters() {
        List<String> errors = new ArrayList<>();
        validationService.validatePhone("024ABC4567", errors);
        assertFalse(errors.isEmpty(), "Expected error for phone with letters");
    }

    @Test
    @DisplayName("Phone too short fails validation")
    void testPhoneTooShort() {
        List<String> errors = new ArrayList<>();
        validationService.validatePhone("024123", errors);
        assertFalse(errors.isEmpty(), "Expected error for phone too short");
    }

    // ========== Full Student Validation ==========

    @Test
    @DisplayName("Fully valid student passes all validation")
    void testValidStudent() {
        Student s = new Student("STU001", "Alice Smith", "Computer Science",
                300, 3.8, "alice@uni.edu", "0241234567");
        List<String> errors = validationService.validate(s);
        assertTrue(errors.isEmpty(), "Expected no errors for fully valid student");
    }

    @Test
    @DisplayName("isValid returns false for invalid student")
    void testIsValidReturnsFalseForInvalidStudent() {
        Student s = new Student();
        s.setStudentId("");        // invalid
        s.setFullName("X");       // too short
        s.setProgramme("");       // required
        s.setLevel(999);          // invalid
        s.setGpa(5.0);            // out of range
        s.setEmail("notvalid");   // missing @ and dot
        s.setPhoneNumber("abc");  // not digits
        assertFalse(validationService.isValid(s));
    }

    @Test
    @DisplayName("GPA threshold validation – valid threshold")
    void testValidGpaThreshold() {
        assertTrue(validationService.isValidGpaThreshold(2.0));
        assertTrue(validationService.isValidGpaThreshold(0.0));
        assertTrue(validationService.isValidGpaThreshold(4.0));
    }

    @Test
    @DisplayName("GPA threshold validation – invalid threshold")
    void testInvalidGpaThreshold() {
        assertFalse(validationService.isValidGpaThreshold(-0.1));
        assertFalse(validationService.isValidGpaThreshold(4.1));
    }
}
