package com.smssimple.repository;

import com.smssimple.domain.Student;
import java.util.List;
import java.util.Optional;

/**
 * Repository interface for student data access.
 * All implementations must use prepared statements.
 */
public interface StudentRepository {

    /**
     * Add a new student record.
     * @param student the student to add
     * @return true if added successfully
     */
    boolean add(Student student);

    /**
     * Find a student by their unique ID.
     * @param studentId the student ID to look up
     * @return Optional containing the student, or empty if not found
     */
    Optional<Student> findById(String studentId);

    /**
     * Retrieve all student records.
     * @return list of all students
     */
    List<Student> findAll();

    /**
     * Update an existing student record.
     * @param student the student with updated fields
     * @return true if updated successfully
     */
    boolean update(Student student);

    /**
     * Delete a student by ID.
     * @param studentId the ID of the student to delete
     * @return true if deleted successfully
     */
    boolean delete(String studentId);

    /**
     * Search students by ID or name (partial match).
     * @param query the search term
     * @return list of matching students
     */
    List<Student> search(String query);

    /**
     * Filter students by programme, level, and/or status.
     * Null values are ignored (not filtered).
     */
    List<Student> filter(String programme, Integer level, String status);

    /**
     * Check if a student ID already exists.
     * @param studentId the ID to check
     * @return true if it exists
     */
    boolean existsById(String studentId);

    /**
     * Get all distinct programme names.
     */
    List<String> getAllProgrammes();
}
