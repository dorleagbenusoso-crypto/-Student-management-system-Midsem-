package com.smssimple.util;

import com.smssimple.domain.Student;
import java.io.*;
import java.nio.file.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Utility class for safe CSV reading and writing operations.
 * All exports are saved inside the data folder as required.
 */
public class CsvHelper {

    private static final String DATA_FOLDER = "data";

    private CsvHelper() {}

    /**
     * Writes a list of students to a CSV file inside the data folder.
     * @param filename the output filename (no path)
     * @param students the list of students to export
     * @throws IOException if writing fails
     */
    public static void exportStudents(String filename, List<Student> students) throws IOException {
        Path folder = Paths.get(DATA_FOLDER);
        Files.createDirectories(folder);
        Path outputPath = folder.resolve(filename);

        try (PrintWriter writer = new PrintWriter(new FileWriter(outputPath.toFile()))) {
            // Header
            writer.println("StudentID,FullName,Programme,Level,GPA,Email,PhoneNumber,DateAdded,Status");
            for (Student s : students) {
                writer.printf("%s,%s,%s,%d,%.2f,%s,%s,%s,%s%n",
                        escape(s.getStudentId()),
                        escape(s.getFullName()),
                        escape(s.getProgramme()),
                        s.getLevel(),
                        s.getGpa(),
                        escape(s.getEmail()),
                        escape(s.getPhoneNumber()),
                        s.getDateAdded(),
                        s.getStatus().name());
            }
        }
        AppLogger.info("Exported " + students.size() + " records to " + outputPath);
    }

    /**
     * Writes error lines to a CSV error report inside the data folder.
     */
    public static void exportErrors(String filename, List<String> errors) throws IOException {
        Path folder = Paths.get(DATA_FOLDER);
        Files.createDirectories(folder);
        Path outputPath = folder.resolve(filename);

        try (PrintWriter writer = new PrintWriter(new FileWriter(outputPath.toFile()))) {
            writer.println("Row,Error");
            for (String error : errors) {
                writer.println(escape(error));
            }
        }
        AppLogger.info("Error report written: " + outputPath);
    }

    /**
     * Reads lines from a CSV file, skipping the header.
     * @param file the CSV file to read
     * @return list of data rows (as raw string arrays)
     */
    public static List<String[]> readCsv(File file) throws IOException {
        List<String[]> rows = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            boolean firstLine = true;
            while ((line = reader.readLine()) != null) {
                if (firstLine) { firstLine = false; continue; } // skip header
                if (!line.isBlank()) {
                    rows.add(parseCsvLine(line));
                }
            }
        }
        return rows;
    }

    /**
     * Simple CSV line parser that handles basic quoted fields.
     */
    public static String[] parseCsvLine(String line) {
        List<String> fields = new ArrayList<>();
        StringBuilder sb = new StringBuilder();
        boolean inQuotes = false;
        for (char c : line.toCharArray()) {
            if (c == '"') {
                inQuotes = !inQuotes;
            } else if (c == ',' && !inQuotes) {
                fields.add(sb.toString().trim());
                sb.setLength(0);
            } else {
                sb.append(c);
            }
        }
        fields.add(sb.toString().trim());
        return fields.toArray(new String[0]);
    }

    /**
     * Escapes a value for safe CSV output.
     */
    private static String escape(String value) {
        if (value == null) return "";
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }

    public static String getDataFolder() { return DATA_FOLDER; }
}
