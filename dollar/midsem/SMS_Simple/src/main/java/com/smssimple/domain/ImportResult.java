package com.smssimple.domain;

import java.util.ArrayList;
import java.util.List;

/**
 * Holds the result of a CSV import operation.
 */
public class ImportResult {

    private int successCount;
    private int errorCount;
    private List<String> errors;

    public ImportResult() {
        this.errors = new ArrayList<>();
    }

    public void incrementSuccess() { successCount++; }
    public void addError(String error) {
        errors.add(error);
        errorCount++;
    }

    public int getSuccessCount() { return successCount; }
    public int getErrorCount() { return errorCount; }
    public List<String> getErrors() { return errors; }

    @Override
    public String toString() {
        return "ImportResult{success=" + successCount + ", errors=" + errorCount + "}";
    }
}
