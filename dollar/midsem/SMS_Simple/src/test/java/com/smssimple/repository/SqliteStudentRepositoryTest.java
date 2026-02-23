package com.smssimple.repository;

import com.smssimple.domain.Student;
import com.smssimple.util.DatabaseManager;
import org.junit.jupiter.api.*;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for SqliteStudentRepository.
 * Uses an in-memory-style test database (real SQLite, wiped before each test).
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class SqliteStudentRepositoryTest {

    private SqliteStudentRepository repository;

    @BeforeEach
    void setUp() throws SQLException {
        repository = new SqliteStudentRepository();
        clearTable(); // Reset before each test
    }

    @AfterAll
    static void tearDownAll() {
        DatabaseManager.close();
    }

    private void clearTable() throws SQLException {
        try (Connection conn = DatabaseManager.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute("DELETE FROM students");
        }
    }

    private Student buildTestStudent(String id) {
        Student s = new Student(id, "Test Student", "Computer Science",
                300, 3.0, id + "@uni.edu", "0241234567");
        return s;
    }

    // ========== Add Tests ==========

    @Test
    @Order(1)
    @DisplayName("Add student returns true and student is retrievable")
    void testAddStudent() {
        Student s = buildTestStudent("TST001");
        boolean added = repository.add(s);
        assertTrue(added, "Expected add to return true");

        Optional<Student> found = repository.findById("TST001");
        assertTrue(found.isPresent(), "Expected student to be found after add");
        assertEquals("Test Student", found.get().getFullName());
    }

    @Test
    @Order(2)
    @DisplayName("Add duplicate student ID returns false")
    void testAddDuplicateId() {
        Student s1 = buildTestStudent("TST002");
        Student s2 = buildTestStudent("TST002"); // same ID
        repository.add(s1);
        boolean result = repository.add(s2);
        assertFalse(result, "Expected duplicate add to return false");
    }

    // ========== Find Tests ==========

    @Test
    @Order(3)
    @DisplayName("Find by ID returns empty for non-existent student")
    void testFindByIdNotFound() {
        Optional<Student> found = repository.findById("NONEXISTENT");
        assertFalse(found.isPresent(), "Expected empty Optional for non-existent ID");
    }

    @Test
    @Order(4)
    @DisplayName("Find all returns all added students")
    void testFindAll() {
        repository.add(buildTestStudent("TST010"));
        repository.add(buildTestStudent("TST011"));
        repository.add(buildTestStudent("TST012"));

        List<Student> all = repository.findAll();
        assertEquals(3, all.size(), "Expected 3 students in the list");
    }

    // ========== Update Tests ==========

    @Test
    @Order(5)
    @DisplayName("Update student changes the stored values")
    void testUpdateStudent() {
        Student s = buildTestStudent("TST020");
        repository.add(s);

        s.setFullName("Updated Name");
        s.setGpa(3.9);
        boolean updated = repository.update(s);
        assertTrue(updated, "Expected update to return true");

        Optional<Student> found = repository.findById("TST020");
        assertTrue(found.isPresent());
        assertEquals("Updated Name", found.get().getFullName());
        assertEquals(3.9, found.get().getGpa(), 0.001);
    }

    // ========== Delete Tests ==========

    @Test
    @Order(6)
    @DisplayName("Delete student removes the record")
    void testDeleteStudent() {
        Student s = buildTestStudent("TST030");
        repository.add(s);
        boolean deleted = repository.delete("TST030");
        assertTrue(deleted, "Expected delete to return true");

        Optional<Student> found = repository.findById("TST030");
        assertFalse(found.isPresent(), "Expected student to be gone after deletion");
    }

    @Test
    @Order(7)
    @DisplayName("Delete non-existent student returns false")
    void testDeleteNonExistent() {
        boolean deleted = repository.delete("GHOST999");
        assertFalse(deleted, "Expected false when deleting non-existent student");
    }

    // ========== Search Tests ==========

    @Test
    @Order(8)
    @DisplayName("Search by name returns matching students")
    void testSearchByName() {
        Student s = buildTestStudent("TST040");
        s.setFullName("Kwame Mensah");
        repository.add(s);

        List<Student> results = repository.search("Mensah");
        assertFalse(results.isEmpty(), "Expected search to find Kwame Mensah");
        assertTrue(results.stream().anyMatch(r -> r.getStudentId().equals("TST040")));
    }

    @Test
    @Order(9)
    @DisplayName("Search by student ID returns matching students")
    void testSearchById() {
        repository.add(buildTestStudent("TST050"));
        List<Student> results = repository.search("TST050");
        assertFalse(results.isEmpty(), "Expected search to find TST050");
    }

    // ========== existsById Tests ==========

    @Test
    @Order(10)
    @DisplayName("existsById returns true for existing student")
    void testExistsById() {
        repository.add(buildTestStudent("TST060"));
        assertTrue(repository.existsById("TST060"));
    }

    @Test
    @Order(11)
    @DisplayName("existsById returns false for non-existing student")
    void testNotExistsById() {
        assertFalse(repository.existsById("NOTHERE"));
    }

    // ========== Filter Tests ==========

    @Test
    @Order(12)
    @DisplayName("Filter by programme returns correct students")
    void testFilterByProgramme() {
        Student s1 = buildTestStudent("TST070");
        s1.setProgramme("Mathematics");
        Student s2 = buildTestStudent("TST071");
        s2.setProgramme("Physics");
        repository.add(s1);
        repository.add(s2);

        List<Student> results = repository.filter("Mathematics", null, null);
        assertTrue(results.stream().allMatch(s -> s.getProgramme().equals("Mathematics")),
                "All filtered results should be from Mathematics");
    }
}
