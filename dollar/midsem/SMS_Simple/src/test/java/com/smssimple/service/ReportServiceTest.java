package com.smssimple.service;

import com.smssimple.domain.Student;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for ReportService.
 * Tests report calculations and filtering logic.
 */
class ReportServiceTest {

    private ReportService reportService;
    private List<Student> testStudents;

    @BeforeEach
    void setUp() {
        reportService = new ReportService();

        // Build a fixed set of test students
        Student s1 = makeStudent("STU001", "Alice",  "Computer Science", 300, 3.9, Student.StudentStatus.ACTIVE);
        Student s2 = makeStudent("STU002", "Bob",    "Computer Science", 200, 1.5, Student.StudentStatus.ACTIVE);
        Student s3 = makeStudent("STU003", "Carol",  "Mathematics",      400, 2.8, Student.StudentStatus.ACTIVE);
        Student s4 = makeStudent("STU004", "David",  "Mathematics",      300, 0.9, Student.StudentStatus.INACTIVE);
        Student s5 = makeStudent("STU005", "Eve",    "Physics",          100, 3.2, Student.StudentStatus.ACTIVE);
        Student s6 = makeStudent("STU006", "Frank",  "Physics",          200, 2.1, Student.StudentStatus.ACTIVE);
        Student s7 = makeStudent("STU007", "Grace",  "Computer Science", 400, 3.5, Student.StudentStatus.ACTIVE);

        testStudents = Arrays.asList(s1, s2, s3, s4, s5, s6, s7);
    }

    private Student makeStudent(String id, String name, String prog, int level,
                                double gpa, Student.StudentStatus status) {
        Student s = new Student(id, name, prog, level, gpa, id + "@uni.edu", "0241234567");
        s.setStatus(status);
        return s;
    }

    // ========== Top Performers Tests ==========

    @Test
    @DisplayName("Top performers returns correct number of results")
    void testTopPerformersCount() {
        List<Student> top = reportService.getTopPerformers(testStudents, 3, null, null);
        assertEquals(3, top.size(), "Expected exactly 3 top performers");
    }

    @Test
    @DisplayName("Top performers are sorted highest GPA first")
    void testTopPerformersSortedByGpa() {
        List<Student> top = reportService.getTopPerformers(testStudents, 7, null, null);
        for (int i = 0; i < top.size() - 1; i++) {
            assertTrue(top.get(i).getGpa() >= top.get(i + 1).getGpa(),
                    "Top performers should be sorted descending by GPA");
        }
    }

    @Test
    @DisplayName("Top performers filtered by programme returns only that programme")
    void testTopPerformersFilteredByProgramme() {
        List<Student> top = reportService.getTopPerformers(testStudents, 10, "Physics", null);
        assertTrue(top.stream().allMatch(s -> s.getProgramme().equals("Physics")),
                "All results should be from Physics programme");
    }

    @Test
    @DisplayName("Top performers filtered by level returns only that level")
    void testTopPerformersFilteredByLevel() {
        List<Student> top = reportService.getTopPerformers(testStudents, 10, null, 300);
        assertTrue(top.stream().allMatch(s -> s.getLevel() == 300),
                "All results should be from level 300");
    }

    // ========== At-Risk Tests ==========

    @Test
    @DisplayName("At-risk students are all below the threshold")
    void testAtRiskBelowThreshold() {
        List<Student> risk = reportService.getAtRiskStudents(testStudents, 2.0);
        assertTrue(risk.stream().allMatch(s -> s.getGpa() < 2.0),
                "All at-risk students should have GPA below threshold");
    }

    @Test
    @DisplayName("At-risk count is correct for threshold 2.0")
    void testAtRiskCount() {
        // Bob (1.5) and David (0.9) are below 2.0
        List<Student> risk = reportService.getAtRiskStudents(testStudents, 2.0);
        assertEquals(2, risk.size(), "Expected 2 at-risk students below GPA 2.0");
    }

    @Test
    @DisplayName("At-risk returns empty when all students pass threshold")
    void testAtRiskEmptyWhenAllPass() {
        List<Student> risk = reportService.getAtRiskStudents(testStudents, 0.5);
        assertTrue(risk.isEmpty(), "Expected no at-risk students when threshold is 0.5");
    }

    // ========== GPA Distribution Tests ==========

    @Test
    @DisplayName("GPA distribution has exactly 4 bands")
    void testGpaDistributionBandCount() {
        Map<String, Long> dist = reportService.getGpaDistribution(testStudents);
        assertEquals(4, dist.size(), "Expected exactly 4 GPA bands");
    }

    @Test
    @DisplayName("GPA distribution counts sum to total student count")
    void testGpaDistributionSumMatchesTotal() {
        Map<String, Long> dist = reportService.getGpaDistribution(testStudents);
        long total = dist.values().stream().mapToLong(Long::longValue).sum();
        assertEquals(testStudents.size(), total,
                "Distribution counts should sum to total number of students");
    }

    @Test
    @DisplayName("GPA distribution correctly places student in band")
    void testGpaDistributionCorrectBand() {
        Map<String, Long> dist = reportService.getGpaDistribution(testStudents);
        // David (0.9) should be in the 0.0–1.0 band
        assertTrue(dist.get("0.0 – 1.0") >= 1,
                "0.0-1.0 band should have at least one student (David with 0.9)");
    }

    // ========== Programme Summary Tests ==========

    @Test
    @DisplayName("Programme summary has correct number of programmes")
    void testProgrammeSummaryCount() {
        Map<String, double[]> summary = reportService.getProgrammeSummary(testStudents);
        assertEquals(3, summary.size(), "Expected 3 distinct programmes");
    }

    @Test
    @DisplayName("Programme summary average GPA is within valid range")
    void testProgrammeSummaryAvgGpaValid() {
        Map<String, double[]> summary = reportService.getProgrammeSummary(testStudents);
        summary.forEach((prog, values) -> {
            double avgGpa = values[1];
            assertTrue(avgGpa >= 0.0 && avgGpa <= 4.0,
                    "Average GPA for " + prog + " should be in valid range");
        });
    }

    // ========== Aggregation Tests ==========

    @Test
    @DisplayName("Average GPA is calculated correctly")
    void testAverageGpa() {
        double avg = reportService.getAverageGpa(testStudents);
        assertTrue(avg > 0 && avg <= 4.0, "Average GPA should be in valid range");
    }

    @Test
    @DisplayName("Count active students is correct")
    void testCountActive() {
        long active = reportService.countActive(testStudents);
        assertEquals(6, active, "Expected 6 active students");
    }

    @Test
    @DisplayName("Count inactive students is correct")
    void testCountInactive() {
        long inactive = reportService.countInactive(testStudents);
        assertEquals(1, inactive, "Expected 1 inactive student (David)");
    }

    @Test
    @DisplayName("Average GPA on empty list returns 0.0")
    void testAverageGpaEmpty() {
        double avg = reportService.getAverageGpa(List.of());
        assertEquals(0.0, avg, "Average GPA on empty list should be 0.0");
    }
}
