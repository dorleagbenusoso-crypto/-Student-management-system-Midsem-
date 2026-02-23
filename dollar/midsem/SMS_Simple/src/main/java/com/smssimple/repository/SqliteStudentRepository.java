package com.smssimple.repository;

import com.smssimple.domain.Student;
import com.smssimple.util.AppLogger;
import com.smssimple.util.DatabaseManager;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * SQLite implementation of StudentRepository.
 * All queries use prepared statements. No SQL is built by concatenating user input.
 */
public class SqliteStudentRepository implements StudentRepository {

    @Override
    public boolean add(Student student) {
        String sql = """
            INSERT INTO students
                (student_id, full_name, programme, level, gpa, email, phone_number, date_added, status)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
            """;
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, student.getStudentId());
            stmt.setString(2, student.getFullName());
            stmt.setString(3, student.getProgramme());
            stmt.setInt(4, student.getLevel());
            stmt.setDouble(5, student.getGpa());
            stmt.setString(6, student.getEmail());
            stmt.setString(7, student.getPhoneNumber());
            stmt.setString(8, student.getDateAdded().toString());
            stmt.setString(9, student.getStatus().name());
            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            AppLogger.error("DB error adding student: " + e.getMessage());
            return false;
        }
    }

    @Override
    public Optional<Student> findById(String studentId) {
        String sql = "SELECT * FROM students WHERE student_id = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, studentId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) return Optional.of(mapRow(rs));

        } catch (SQLException e) {
            AppLogger.error("DB error finding student by ID: " + e.getMessage());
        }
        return Optional.empty();
    }

    @Override
    public List<Student> findAll() {
        String sql = "SELECT * FROM students ORDER BY full_name ASC";
        List<Student> students = new ArrayList<>();
        try (Connection conn = DatabaseManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) students.add(mapRow(rs));

        } catch (SQLException e) {
            AppLogger.error("DB error fetching all students: " + e.getMessage());
        }
        return students;
    }

    @Override
    public boolean update(Student student) {
        String sql = """
            UPDATE students SET
                full_name = ?, programme = ?, level = ?, gpa = ?,
                email = ?, phone_number = ?, status = ?
            WHERE student_id = ?
            """;
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, student.getFullName());
            stmt.setString(2, student.getProgramme());
            stmt.setInt(3, student.getLevel());
            stmt.setDouble(4, student.getGpa());
            stmt.setString(5, student.getEmail());
            stmt.setString(6, student.getPhoneNumber());
            stmt.setString(7, student.getStatus().name());
            stmt.setString(8, student.getStudentId());
            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            AppLogger.error("DB error updating student: " + e.getMessage());
            return false;
        }
    }

    @Override
    public boolean delete(String studentId) {
        String sql = "DELETE FROM students WHERE student_id = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, studentId);
            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            AppLogger.error("DB error deleting student: " + e.getMessage());
            return false;
        }
    }

    @Override
    public List<Student> search(String query) {
        String sql = "SELECT * FROM students WHERE student_id LIKE ? OR full_name LIKE ?";
        List<Student> students = new ArrayList<>();
        String pattern = "%" + query + "%";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, pattern);
            stmt.setString(2, pattern);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) students.add(mapRow(rs));

        } catch (SQLException e) {
            AppLogger.error("DB error searching students: " + e.getMessage());
        }
        return students;
    }

    @Override
    public List<Student> filter(String programme, Integer level, String status) {
        StringBuilder sql = new StringBuilder("SELECT * FROM students WHERE 1=1");
        List<Object> params = new ArrayList<>();

        if (programme != null && !programme.isBlank()) {
            sql.append(" AND programme = ?");
            params.add(programme);
        }
        if (level != null) {
            sql.append(" AND level = ?");
            params.add(level);
        }
        if (status != null && !status.isBlank()) {
            sql.append(" AND status = ?");
            params.add(status);
        }
        sql.append(" ORDER BY full_name ASC");

        List<Student> students = new ArrayList<>();
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql.toString())) {

            for (int i = 0; i < params.size(); i++) {
                stmt.setObject(i + 1, params.get(i));
            }
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) students.add(mapRow(rs));

        } catch (SQLException e) {
            AppLogger.error("DB error filtering students: " + e.getMessage());
        }
        return students;
    }

    @Override
    public boolean existsById(String studentId) {
        String sql = "SELECT 1 FROM students WHERE student_id = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, studentId);
            return stmt.executeQuery().next();

        } catch (SQLException e) {
            AppLogger.error("DB error checking student existence: " + e.getMessage());
            return false;
        }
    }

    @Override
    public List<String> getAllProgrammes() {
        String sql = "SELECT DISTINCT programme FROM students ORDER BY programme ASC";
        List<String> programmes = new ArrayList<>();
        try (Connection conn = DatabaseManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) programmes.add(rs.getString("programme"));

        } catch (SQLException e) {
            AppLogger.error("DB error fetching programmes: " + e.getMessage());
        }
        return programmes;
    }

    /** Maps a ResultSet row to a Student object. */
    private Student mapRow(ResultSet rs) throws SQLException {
        Student s = new Student();
        s.setStudentId(rs.getString("student_id"));
        s.setFullName(rs.getString("full_name"));
        s.setProgramme(rs.getString("programme"));
        s.setLevel(rs.getInt("level"));
        s.setGpa(rs.getDouble("gpa"));
        s.setEmail(rs.getString("email"));
        s.setPhoneNumber(rs.getString("phone_number"));
        s.setDateAdded(LocalDate.parse(rs.getString("date_added")));
        s.setStatus(Student.StudentStatus.valueOf(rs.getString("status")));
        return s;
    }
}
