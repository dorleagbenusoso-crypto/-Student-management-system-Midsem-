package com.smssimple.ui;

import com.smssimple.domain.ImportResult;
import com.smssimple.domain.Student;
import com.smssimple.repository.SqliteStudentRepository;
import com.smssimple.service.ReportService;
import com.smssimple.service.StudentService;
import com.smssimple.service.ValidationService;
import com.smssimple.util.AppLogger;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.stage.FileChooser;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.*;

public class MainController implements Initializable {

    private final StudentService studentService;
    private final ReportService reportService;
    private final ValidationService validationService;
    
    private double atRiskThreshold = 2.0;
    private File selectedImportFile;
    private String currentReportType = "top";

    // Dashboard
    @FXML private Label lblTotal, lblActive, lblInactive, lblAvgGpa;

    // Students
    @FXML private TextField txtSearch;
    @FXML private ComboBox<String> cboProgramme, cboLevel, cboStatus;
    @FXML private TableView<Student> studentTable;
    @FXML private TableColumn<Student, String> colId, colName, colProgramme, colEmail, colPhone, colStatus;
    @FXML private TableColumn<Student, Integer> colLevel;
    @FXML private TableColumn<Student, Double> colGpa;
    @FXML private Label lblCount;

    // Reports
    @FXML private Label lblReportTitle;
    @FXML private TableView<?> reportTable;

    // Import/Export
    @FXML private TextField txtImportFile;
    @FXML private Label lblImportStatus, lblExportStatus;

    // Settings
    @FXML private TextField txtThreshold;
    @FXML private Label lblThresholdMsg;

    // Status
    @FXML private Label lblStatus;

    private ObservableList<Student> studentData;

    public MainController() {
        this.studentService = new StudentService(new SqliteStudentRepository());
        this.reportService = new ReportService();
        this.validationService = new ValidationService();
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        initStudentTable();
        initFilters();
        loadStudents();
        refreshDashboard();
        setStatus("Ready");
        AppLogger.info("Application started");
    }

    // ========== STUDENT TABLE ==========

    private void initStudentTable() {
        colId.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getStudentId()));
        colName.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getFullName()));
        colProgramme.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getProgramme()));
        colLevel.setCellValueFactory(d -> new SimpleIntegerProperty(d.getValue().getLevel()).asObject());
        colGpa.setCellValueFactory(d -> new SimpleDoubleProperty(d.getValue().getGpa()).asObject());
        colEmail.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getEmail()));
        colPhone.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getPhoneNumber()));
        colStatus.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getStatus().name()));

        studentData = FXCollections.observableArrayList();
        studentTable.setItems(studentData);
    }

    private void initFilters() {
        cboLevel.setItems(FXCollections.observableArrayList("All", "100", "200", "300", "400", "500", "600", "700"));
        cboStatus.setItems(FXCollections.observableArrayList("All", "ACTIVE", "INACTIVE"));
        cboProgramme.getItems().add("All");
        
        cboLevel.setValue("All");
        cboStatus.setValue("All");
        cboProgramme.setValue("All");
    }

    private void loadStudents() {
        List<Student> students = studentService.getAllStudents();
        studentData.setAll(students);
        lblCount.setText(students.size() + " students");
        refreshProgrammeCombo();
        refreshDashboard();
    }

    private void refreshProgrammeCombo() {
        String current = cboProgramme.getValue();
        cboProgramme.getItems().clear();
        cboProgramme.getItems().add("All");
        cboProgramme.getItems().addAll(studentService.getAllProgrammes());
        cboProgramme.setValue(current != null ? current : "All");
    }

    @FXML private void onSearch() {
        String query = txtSearch.getText().trim();
        if (query.isEmpty()) {
            loadStudents();
        } else {
            List<Student> results = studentService.searchStudents(query);
            studentData.setAll(results);
            lblCount.setText(results.size() + " students");
            setStatus("Found " + results.size() + " result(s)");
        }
    }

    @FXML private void onClear() {
        txtSearch.clear();
        cboProgramme.setValue("All");
        cboLevel.setValue("All");
        cboStatus.setValue("All");
        loadStudents();
        setStatus("Filters cleared");
    }

    @FXML private void onFilter() {
        String prog = cboProgramme.getValue();
        String lvl = cboLevel.getValue();
        String stat = cboStatus.getValue();

        String progFilter = (prog == null || prog.equals("All")) ? null : prog;
        Integer levelFilter = (lvl == null || lvl.equals("All")) ? null : Integer.parseInt(lvl);
        String statusFilter = (stat == null || stat.equals("All")) ? null : stat;

        List<Student> filtered = studentService.filterStudents(progFilter, levelFilter, statusFilter);
        studentData.setAll(filtered);
        lblCount.setText(filtered.size() + " students");
        setStatus("Filter applied");
    }

    // ========== CRUD ==========

    @FXML private void onAddStudent() {
        showStudentDialog(null);
    }

    @FXML private void onEditStudent() {
        Student selected = studentTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert(Alert.AlertType.WARNING, "No Selection", "Please select a student to edit");
            return;
        }
        showStudentDialog(selected);
    }

    @FXML private void onDeleteStudent() {
        Student selected = studentTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert(Alert.AlertType.WARNING, "No Selection", "Please select a student to delete");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirm Delete");
        confirm.setHeaderText("Delete " + selected.getFullName() + "?");
        confirm.setContentText("This cannot be undone.");
        
        if (confirm.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            if (studentService.deleteStudent(selected.getStudentId())) {
                loadStudents();
                setStatus("Student deleted");
            } else {
                showAlert(Alert.AlertType.ERROR, "Error", "Failed to delete student");
            }
        }
    }

    private void showStudentDialog(Student existing) {
        Dialog<Student> dialog = new Dialog<>();
        dialog.setTitle(existing == null ? "Add New Student" : "Edit Student");
        
        ButtonType saveButtonType = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20));

        TextField fId = new TextField();
        TextField fName = new TextField();
        TextField fProgramme = new TextField();
        ComboBox<Integer> fLevel = new ComboBox<>(FXCollections.observableArrayList(100, 200, 300, 400, 500, 600, 700));
        TextField fGpa = new TextField();
        TextField fEmail = new TextField();
        TextField fPhone = new TextField();
        ComboBox<String> fStatus = new ComboBox<>(FXCollections.observableArrayList("ACTIVE", "INACTIVE"));

        if (existing != null) {
            fId.setText(existing.getStudentId());
            fId.setEditable(false);
            fName.setText(existing.getFullName());
            fProgramme.setText(existing.getProgramme());
            fLevel.setValue(existing.getLevel());
            fGpa.setText(String.valueOf(existing.getGpa()));
            fEmail.setText(existing.getEmail());
            fPhone.setText(existing.getPhoneNumber());
            fStatus.setValue(existing.getStatus().name());
        } else {
            fLevel.setValue(100);
            fStatus.setValue("ACTIVE");
        }

        grid.add(new Label("Student ID:"), 0, 0);
        grid.add(fId, 1, 0);
        grid.add(new Label("Full Name:"), 0, 1);
        grid.add(fName, 1, 1);
        grid.add(new Label("Programme:"), 0, 2);
        grid.add(fProgramme, 1, 2);
        grid.add(new Label("Level:"), 0, 3);
        grid.add(fLevel, 1, 3);
        grid.add(new Label("GPA:"), 0, 4);
        grid.add(fGpa, 1, 4);
        grid.add(new Label("Email:"), 0, 5);
        grid.add(fEmail, 1, 5);
        grid.add(new Label("Phone:"), 0, 6);
        grid.add(fPhone, 1, 6);
        grid.add(new Label("Status:"), 0, 7);
        grid.add(fStatus, 1, 7);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(btn -> {
            if (btn == saveButtonType) {
                Student s = existing != null ? existing : new Student();
                s.setStudentId(fId.getText().trim());
                s.setFullName(fName.getText().trim());
                s.setProgramme(fProgramme.getText().trim());
                s.setLevel(fLevel.getValue() != null ? fLevel.getValue() : 100);
                try {
                    s.setGpa(Double.parseDouble(fGpa.getText().trim()));
                } catch (NumberFormatException e) {
                    s.setGpa(-1);
                }
                s.setEmail(fEmail.getText().trim());
                s.setPhoneNumber(fPhone.getText().trim());
                s.setStatus(Student.StudentStatus.valueOf(fStatus.getValue()));
                return s;
            }
            return null;
        });

        dialog.showAndWait().ifPresent(student -> {
            List<String> errors = existing == null 
                ? studentService.addStudent(student)
                : studentService.updateStudent(student);
            
            if (errors.isEmpty()) {
                loadStudents();
                setStatus(existing == null ? "Student added" : "Student updated");
            } else {
                showAlert(Alert.AlertType.ERROR, "Validation Error", String.join("\n", errors));
            }
        });
    }

    // ========== DASHBOARD ==========

    private void refreshDashboard() {
        List<Student> all = studentService.getAllStudents();
        lblTotal.setText(String.valueOf(all.size()));
        lblActive.setText(String.valueOf(reportService.countActive(all)));
        lblInactive.setText(String.valueOf(reportService.countInactive(all)));
        lblAvgGpa.setText(String.format("%.2f", reportService.getAverageGpa(all)));
    }

    // ========== REPORTS ==========

    @FXML private void showTopPerformers() {
        currentReportType = "top";
        List<Student> all = studentService.getAllStudents();
        List<Student> top = reportService.getTopPerformers(all, 10, null, null);
        lblReportTitle.setText("Top 10 Students by GPA");
        displayStudentReport(top);
    }

    @FXML private void showAtRisk() {
        currentReportType = "risk";
        List<Student> all = studentService.getAllStudents();
        List<Student> risk = reportService.getAtRiskStudents(all, atRiskThreshold);
        lblReportTitle.setText("At-Risk Students (GPA < " + atRiskThreshold + ")");
        displayStudentReport(risk);
    }

    @FXML private void showGpaDist() {
        currentReportType = "dist";
        lblReportTitle.setText("GPA Distribution");
        displayGpaDistribution();
    }

    @FXML private void showProgSummary() {
        currentReportType = "prog";
        lblReportTitle.setText("Programme Summary");
        displayProgrammeSummary();
    }

    @SuppressWarnings("unchecked")
    private void displayStudentReport(List<Student> students) {
        TableView<Student> tv = (TableView<Student>) reportTable;
        tv.getColumns().clear();

        TableColumn<Student, String> c1 = new TableColumn<>("Student ID");
        c1.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getStudentId()));
        c1.setPrefWidth(120);

        TableColumn<Student, String> c2 = new TableColumn<>("Full Name");
        c2.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getFullName()));
        c2.setPrefWidth(180);

        TableColumn<Student, String> c3 = new TableColumn<>("Programme");
        c3.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getProgramme()));
        c3.setPrefWidth(150);

        TableColumn<Student, Integer> c4 = new TableColumn<>("Level");
        c4.setCellValueFactory(d -> new SimpleIntegerProperty(d.getValue().getLevel()).asObject());
        c4.setPrefWidth(70);

        TableColumn<Student, Double> c5 = new TableColumn<>("GPA");
        c5.setCellValueFactory(d -> new SimpleDoubleProperty(d.getValue().getGpa()).asObject());
        c5.setPrefWidth(70);

        tv.getColumns().addAll(c1, c2, c3, c4, c5);
        tv.setItems(FXCollections.observableArrayList(students));
    }

    @SuppressWarnings("unchecked")
    private void displayGpaDistribution() {
        TableView<String[]> tv = (TableView<String[]>) (TableView<?>) reportTable;
        tv.getColumns().clear();

        TableColumn<String[], String> c1 = new TableColumn<>("GPA Band");
        c1.setCellValueFactory(d -> new SimpleStringProperty(d.getValue()[0]));
        c1.setPrefWidth(200);

        TableColumn<String[], String> c2 = new TableColumn<>("Student Count");
        c2.setCellValueFactory(d -> new SimpleStringProperty(d.getValue()[1]));
        c2.setPrefWidth(150);

        tv.getColumns().addAll(c1, c2);

        Map<String, Long> dist = reportService.getGpaDistribution(studentService.getAllStudents());
        ObservableList<String[]> data = FXCollections.observableArrayList();
        dist.forEach((k, v) -> data.add(new String[]{k, String.valueOf(v)}));
        tv.setItems(data);
    }

    @SuppressWarnings("unchecked")
    private void displayProgrammeSummary() {
        TableView<String[]> tv = (TableView<String[]>) (TableView<?>) reportTable;
        tv.getColumns().clear();

        TableColumn<String[], String> c1 = new TableColumn<>("Programme");
        c1.setCellValueFactory(d -> new SimpleStringProperty(d.getValue()[0]));
        c1.setPrefWidth(220);

        TableColumn<String[], String> c2 = new TableColumn<>("Total Students");
        c2.setCellValueFactory(d -> new SimpleStringProperty(d.getValue()[1]));
        c2.setPrefWidth(120);

        TableColumn<String[], String> c3 = new TableColumn<>("Avg GPA");
        c3.setCellValueFactory(d -> new SimpleStringProperty(d.getValue()[2]));
        c3.setPrefWidth(100);

        tv.getColumns().addAll(c1, c2, c3);

        Map<String, double[]> summary = reportService.getProgrammeSummary(studentService.getAllStudents());
        ObservableList<String[]> data = FXCollections.observableArrayList();
        summary.forEach((k, v) -> data.add(new String[]{k, String.valueOf((int) v[0]), String.format("%.2f", v[1])}));
        tv.setItems(data);
    }

    @FXML private void onExportReport() {
        List<Student> all = studentService.getAllStudents();
        String filename = switch(currentReportType) {
            case "top" -> "top_performers.csv";
            case "risk" -> "at_risk.csv";
            default -> "report.csv";
        };
        
        List<Student> data = switch(currentReportType) {
            case "top" -> reportService.getTopPerformers(all, 10, null, null);
            case "risk" -> reportService.getAtRiskStudents(all, atRiskThreshold);
            default -> all;
        };
        
        exportStudents(filename, data);
    }

    // ========== IMPORT/EXPORT ==========

    @FXML private void onBrowseImport() {
        FileChooser fc = new FileChooser();
        fc.setTitle("Select CSV File");
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV Files", "*.csv"));
        File file = fc.showOpenDialog(null);
        if (file != null) {
            selectedImportFile = file;
            txtImportFile.setText(file.getAbsolutePath());
        }
    }

    @FXML private void onImport() {
        if (selectedImportFile == null) {
            showAlert(Alert.AlertType.WARNING, "No File", "Please select a CSV file first");
            return;
        }

        Task<ImportResult> task = new Task<>() {
            @Override
            protected ImportResult call() {
                return studentService.importFromCsv(selectedImportFile);
            }
        };

        task.setOnSucceeded(e -> {
            ImportResult result = task.getValue();
            lblImportStatus.setText("Success: " + result.getSuccessCount() + " | Errors: " + result.getErrorCount());
            if (result.getErrorCount() > 0) {
                lblImportStatus.setText(lblImportStatus.getText() + " (see data/import_errors.csv)");
            }
            loadStudents();
            setStatus("Import completed");
        });

        task.setOnFailed(e -> {
            lblImportStatus.setText("Import failed: " + task.getException().getMessage());
        });

        new Thread(task).start();
    }

    @FXML private void onExportAll() {
        exportStudents("all_students.csv", studentService.getAllStudents());
    }

    @FXML private void onExportTop() {
        List<Student> top = reportService.getTopPerformers(studentService.getAllStudents(), 10, null, null);
        exportStudents("top_performers.csv", top);
    }

    @FXML private void onExportRisk() {
        List<Student> risk = reportService.getAtRiskStudents(studentService.getAllStudents(), atRiskThreshold);
        exportStudents("at_risk_students.csv", risk);
    }

    private void exportStudents(String filename, List<Student> students) {
        Task<Void> task = new Task<>() {
            @Override
            protected Void call() throws IOException {
                studentService.exportListToCsv(filename, students);
                return null;
            }
        };

        task.setOnSucceeded(e -> {
            lblExportStatus.setText("Exported to data/" + filename);
            setStatus("Export completed");
        });

        task.setOnFailed(e -> {
            lblExportStatus.setText("Export failed: " + task.getException().getMessage());
        });

        new Thread(task).start();
    }

    // ========== SETTINGS ==========

    @FXML private void onSaveThreshold() {
        try {
            double value = Double.parseDouble(txtThreshold.getText().trim());
            if (!validationService.isValidGpaThreshold(value)) {
                lblThresholdMsg.setText("Threshold must be between 0.0 and 4.0");
                return;
            }
            atRiskThreshold = value;
            lblThresholdMsg.setText("Saved successfully");
            AppLogger.info("At-risk threshold set to " + value);
        } catch (NumberFormatException e) {
            lblThresholdMsg.setText("Please enter a valid number");
        }
    }

    // ========== MENU ==========

    @FXML private void onExit() {
        System.exit(0);
    }

    @FXML private void onAbout() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("About");
        alert.setHeaderText("Student Management System");
        alert.setContentText("Version 1.0\nBuilt with JavaFX and SQLite");
        alert.showAndWait();
    }

    // ========== HELPERS ==========

    private void setStatus(String message) {
        if (lblStatus != null) {
            lblStatus.setText(message);
        }
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
