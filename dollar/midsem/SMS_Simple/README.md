# Student Management System - Simple Version

Basic offline desktop application for managing student records.

**Version:** 1.0.0  
**Platform:** Windows Offline

---

## How to Run in IntelliJ IDEA

### Step 1: Open the Project

1. Open IntelliJ IDEA
2. Click `File → Open`
3. Navigate to this folder and select the **`pom.xml`** file
4. Click **"Open as Project"** when prompted
5. Wait for Maven to download dependencies (requires internet connection once)

### Step 2: Configure Run Settings

1. Go to `Run → Edit Configurations`
2. Click the **+** button and select **Application**
3. Set the following:
   - **Name:** MainApp
   - **Main class:** `com.smssimple.MainApp`
   - **VM options:** `--module-path "C:\javafx\javafx-sdk-21\lib" --add-modules javafx.controls,javafx.fxml`
   - **Working directory:** Select the project root folder (where pom.xml is located)
4. Click **Apply** then **OK**

### Step 3: Run

Click the green **Run** button or press **Shift+F10**

---

## Important Notes

- Make sure the **VM options** path points to where you extracted JavaFX SDK
- The **Working directory** must be the project root folder
- Download JavaFX SDK 21 from: https://gluonhq.com/products/javafx/
- Download JDK 21 from: https://adoptium.net/

---

## Run Tests

```bash
mvn test
```

## Main Class

**`com.smssimple.MainApp`**
