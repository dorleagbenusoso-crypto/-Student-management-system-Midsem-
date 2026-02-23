package com.smssimple.service;

import com.smssimple.domain.ImportResult;
import com.smssimple.domain.Student;
import com.smssimple.repository.StudentRepository;
import com.smssimple.util.AppLogger;
import com.smssimple.util.CsvHelper;
import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

/**
 * Main business logic service for student management.
 * Controllers call this service exclusively.
 * This class does NOT contain SQL; that belongs in the repository.
 */
public class StudentService {

    private final StudentRepository repository;
    private final ValidationService validationService;

    public StudentService(StudentRepository repository) {
        this.repository = repository;
        this.validationService = new ValidationService();
    }

    /**
     * Adds a new student after validation.
     * @return list of errors, empty if success
     */
    public List<String> addStudent(Student student) {
        List<String> errors = validationService.validate(student);
        if (!errors.isEmpty()) return errors;

        if (repository.existsById(student.getStudentId())) {
            errors.add("Student ID '" + student.getStudentId() + "' already exists.");
            return errors;
        }

        student.setDateAdded(LocalDate.now());
        boolean saved = repository.add(student);
        if (!saved) {
            errors.add("Failed to save student. Please try again.");
            AppLogger.error("Failed to add student ID: " + student.getStudentId());
        } else {
            AppLogger.info("Student added: ID=" + student.getStudentId());
        }
        return errors;
    }

    /**
     * Updates a student's record after validation.
     * @return list of errors, empty if success
     */
    public List<String> updateStudent(Student student) {
        List<String> errors = validationService.validate(student);
        if (!errors.isEmpty()) return errors;

        boolean updated = repository.update(student);
        if (!updated) {
            errors.add("Failed to update student. Record may not exist.");
            AppLogger.error("Failed to update student ID: " + student.getStudentId());
        } else {
            AppLogger.info("Student updated: ID=" + student.getStudentId());
        }
        return errors;
    }

    /**
     * Deletes a student by ID.
     * @return true if deleted
     */
    public boolean deleteStudent(String studentId) {
        boolean deleted = repository.delete(studentId);
        if (deleted) {
            AppLogger.info("Student deleted: ID=" + studentId);
        } else {
            AppLogger.error("Failed to delete student ID: " + studentId);
        }
        return deleted;
    }

    public Optional<Student> findById(String studentId) {
        return repository.findById(studentId);
    }

    public List<Student> getAllStudents() {
        return repository.findAll();
    }

    public List<Student> searchStudents(String query) {
        return repository.search(query);
    }

    public List<Student> filterStudents(String programme, Integer level, String status) {
        return repository.filter(programme, level, status);
    }

    public List<Student> getSortedByGpa(List<Student> students, boolean ascending) {
        List<Student> sorted = new ArrayList<>(students);
        if (ascending) {
            sorted.sort(Comparator.comparingDouble(Student::getGpa));
        } else {
            sorted.sort(Comparator.comparingDouble(Student::getGpa).reversed());
        }
        return sorted;
    }

    public List<Student> getSortedByName(List<Student> students) {
        List<Student> sorted = new ArrayList<>(students);
        sorted.sort(Comparator.comparing(Student::getFullName, String.CASE_INSENSITIVE_ORDER));
        return sorted;
    }

    public List<String> getAllProgrammes() {
        return repository.getAllProgrammes();
    }

    /**
     * Imports students from a CSV file.
     * Invalid rows are skipped and recorded. Duplicate IDs are rejected.
     * @param csvFile the CSV file to import
     * @return ImportResult with counts and error messages
     */
    public ImportResult importFromCsv(File csvFile) {
        ImportResult result = new ImportResult();
        List<String[]> rows;

        try {
            rows = CsvHelper.readCsv(csvFile);
        } catch (IOException e) {
            result.addError("Could not read CSV file: " + e.getMessage());
            AppLogger.error("CSV import failed: " + e.getMessage());
            return result;
        }

        int rowNumber = 1; // starts after header
        for (String[] row : rows) {
            rowNumber++;
            try {
                Student student = parseCsvRow(row);
                List<String> errors = validationService.validate(student);
                if (!errors.isEmpty()) {
                    result.addError("Row " + rowNumber + ": " + String.join("; ", errors));
                    continue;
                }
                if (repository.existsById(student.getStudentId())) {
                    result.addError("Row " + rowNumber + ": Duplicate ID '" + student.getStudentId() + "'");
                    continue;
                }
                repository.add(student);
                result.incrementSuccess();
            } catch (Exception e) {
                result.addError("Row " + rowNumber + ": Parse error - " + e.getMessage());
            }
        }

        AppLogger.info("Import complete. Success=" + result.getSuccessCount()
                + " Errors=" + result.getErrorCount());

        // Save error report if there were any errors
        if (result.getErrorCount() > 0) {
            try {
                CsvHelper.exportErrors("import_errors.csv", result.getErrors());
            } catch (IOException e) {
                AppLogger.error("Could not write import error report: " + e.getMessage());
            }
        }

        return result;
    }

    /**
     * Parses a CSV row array into a Student object.
     * Expected column order: StudentID, FullName, Programme, Level, GPA, Email, Phone, DateAdded, Status
     */
    private Student parseCsvRow(String[] row) {
        if (row.length < 8) throw new IllegalArgumentException("Insufficient columns in row");

        Student s = new Student();
        s.setStudentId(row[0].trim());
        s.setFullName(row[1].trim());
        s.setProgramme(row[2].trim());
        s.setLevel(Integer.parseInt(row[3].trim()));
        s.setGpa(Double.parseDouble(row[4].trim()));
        s.setEmail(row[5].trim());
        s.setPhoneNumber(row[6].trim());
        s.setDateAdded(row[7].trim().isEmpty() ? LocalDate.now() : LocalDate.parse(row[7].trim()));
        if (row.length > 8 && !row[8].isBlank()) {
            s.setStatus(Student.StudentStatus.valueOf(row[8].trim().toUpperCase()));
        } else {
            s.setStatus(Student.StudentStatus.ACTIVE);
        }
        return s;
    }

    /**
     * Exports all students to a CSV file inside the data folder.
     */
    public void exportAllToCsv(String filename) throws IOException {
        List<Student> all = repository.findAll();
        CsvHelper.exportStudents(filename, all);
        AppLogger.info("Export complete: " + filename + " (" + all.size() + " records)");
    }

    /**
     * Exports a filtered list to CSV.
     */
    public void exportListToCsv(String filename, List<Student> students) throws IOException {
        CsvHelper.exportStudents(filename, students);
        AppLogger.info("Export complete: " + filename + " (" + students.size() + " records)");
    }
}
