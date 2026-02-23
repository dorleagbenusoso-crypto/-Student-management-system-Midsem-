package com.smssimple.service;

import com.smssimple.domain.Student;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service that generates all report data.
 * All calculations happen here, not in controllers.
 */
public class ReportService {

    /**
     * Returns the top N students by GPA, optionally filtered by programme and level.
     * @param students the full student list
     * @param topN number of top students to return
     * @param programme filter by programme (null = all)
     * @param level filter by level (null = all)
     */
    public List<Student> getTopPerformers(List<Student> students, int topN,
                                          String programme, Integer level) {
        return students.stream()
                .filter(s -> programme == null || programme.isBlank()
                        || s.getProgramme().equalsIgnoreCase(programme))
                .filter(s -> level == null || s.getLevel() == level)
                .sorted(Comparator.comparingDouble(Student::getGpa).reversed())
                .limit(topN)
                .collect(Collectors.toList());
    }

    /**
     * Returns students whose GPA is below the given threshold.
     * @param students the full student list
     * @param threshold the at-risk GPA threshold (default 2.0)
     */
    public List<Student> getAtRiskStudents(List<Student> students, double threshold) {
        return students.stream()
                .filter(s -> s.getGpa() < threshold)
                .sorted(Comparator.comparingDouble(Student::getGpa))
                .collect(Collectors.toList());
    }

    /**
     * Returns a map of GPA band labels to student counts.
     * Bands: 0.0–1.0, 1.0–2.0, 2.0–3.0, 3.0–4.0
     */
    public Map<String, Long> getGpaDistribution(List<Student> students) {
        Map<String, Long> distribution = new LinkedHashMap<>();
        distribution.put("0.0 – 1.0", 0L);
        distribution.put("1.0 – 2.0", 0L);
        distribution.put("2.0 – 3.0", 0L);
        distribution.put("3.0 – 4.0", 0L);

        for (Student s : students) {
            double gpa = s.getGpa();
            if (gpa < 1.0) {
                distribution.merge("0.0 – 1.0", 1L, Long::sum);
            } else if (gpa < 2.0) {
                distribution.merge("1.0 – 2.0", 1L, Long::sum);
            } else if (gpa < 3.0) {
                distribution.merge("2.0 – 3.0", 1L, Long::sum);
            } else {
                distribution.merge("3.0 – 4.0", 1L, Long::sum);
            }
        }
        return distribution;
    }

    /**
     * Returns a map of programme names to a summary array:
     * [totalStudents, averageGpa]
     */
    public Map<String, double[]> getProgrammeSummary(List<Student> students) {
        Map<String, List<Student>> byProgramme = students.stream()
                .collect(Collectors.groupingBy(Student::getProgramme));

        Map<String, double[]> summary = new LinkedHashMap<>();
        for (Map.Entry<String, List<Student>> entry : byProgramme.entrySet()) {
            List<Student> group = entry.getValue();
            double avgGpa = group.stream()
                    .mapToDouble(Student::getGpa)
                    .average()
                    .orElse(0.0);
            summary.put(entry.getKey(), new double[]{ group.size(), avgGpa });
        }
        return summary;
    }

    /**
     * Calculates the average GPA across all students.
     */
    public double getAverageGpa(List<Student> students) {
        if (students.isEmpty()) return 0.0;
        return students.stream()
                .mapToDouble(Student::getGpa)
                .average()
                .orElse(0.0);
    }

    /**
     * Counts students with ACTIVE status.
     */
    public long countActive(List<Student> students) {
        return students.stream()
                .filter(s -> s.getStatus() == Student.StudentStatus.ACTIVE)
                .count();
    }

    /**
     * Counts students with INACTIVE status.
     */
    public long countInactive(List<Student> students) {
        return students.stream()
                .filter(s -> s.getStatus() == Student.StudentStatus.INACTIVE)
                .count();
    }
}
