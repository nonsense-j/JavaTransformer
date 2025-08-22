package com.transformer.api;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Encapsulates bug information for guided transformations.
 * Contains bug detection status and line numbers where bugs were detected.
 */
public class BugInformation {
    
    private boolean hasBugs;
    private List<Integer> bugLines;
    private String filePath;
    
    /**
     * Creates a new BugInformation instance.
     * 
     * @param hasBugs Whether bugs were detected in the code
     * @param bugLines List of line numbers where bugs were detected (can be null)
     */
    public BugInformation(boolean hasBugs, List<Integer> bugLines) {
        this.hasBugs = hasBugs;
        this.bugLines = bugLines != null ? new ArrayList<>(bugLines) : new ArrayList<>();
        validateBugInformation();
    }
    
    /**
     * Creates a new BugInformation instance with file path.
     * 
     * @param hasBugs Whether bugs were detected in the code
     * @param bugLines List of line numbers where bugs were detected (can be null)
     * @param filePath Path to the file being analyzed
     */
    public BugInformation(boolean hasBugs, List<Integer> bugLines, String filePath) {
        this.hasBugs = hasBugs;
        this.bugLines = bugLines != null ? new ArrayList<>(bugLines) : new ArrayList<>();
        this.filePath = filePath;
        validateBugInformation();
    }
    
    /**
     * Creates BugInformation indicating no bugs detected.
     * 
     * @return BugInformation with hasBugs=false and empty bug lines
     */
    public static BugInformation noBugs() {
        return new BugInformation(false, null);
    }
    
    /**
     * Creates BugInformation with bugs detected at specified lines.
     * 
     * @param bugLines List of line numbers where bugs were detected
     * @return BugInformation with hasBugs=true and specified bug lines
     */
    public static BugInformation withBugs(List<Integer> bugLines) {
        return new BugInformation(true, bugLines);
    }
    
    public boolean hasBugs() {
        return hasBugs;
    }
    
    public void setHasBugs(boolean hasBugs) {
        this.hasBugs = hasBugs;
        validateBugInformation();
    }
    
    /**
     * Returns an unmodifiable view of the bug lines list.
     * 
     * @return Unmodifiable list of bug line numbers
     */
    public List<Integer> getBugLines() {
        return Collections.unmodifiableList(bugLines);
    }
    
    public void setBugLines(List<Integer> bugLines) {
        this.bugLines = bugLines != null ? new ArrayList<>(bugLines) : new ArrayList<>();
        validateBugInformation();
    }
    
    public String getFilePath() {
        return filePath;
    }
    
    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }
    
    /**
     * Validates the bug information for consistency.
     * 
     * @throws IllegalArgumentException if validation fails
     */
    private void validateBugInformation() {
        if (hasBugs && (bugLines == null || bugLines.isEmpty())) {
            throw new IllegalArgumentException("When hasBugs is true, bugLines cannot be null or empty");
        }
        
        if (bugLines != null) {
            for (Integer lineNumber : bugLines) {
                if (lineNumber == null || lineNumber <= 0) {
                    throw new IllegalArgumentException("Bug line numbers must be positive integers, found: " + lineNumber);
                }
            }
        }
    }
    
    /**
     * Checks if this bug information is valid for guided transformations.
     * 
     * @return true if valid for guided transformations, false otherwise
     */
    public boolean isValidForGuidedTransformation() {
        return hasBugs && bugLines != null && !bugLines.isEmpty();
    }
    
    /**
     * Returns the number of bug lines.
     * 
     * @return Number of lines with detected bugs
     */
    public int getBugLineCount() {
        return bugLines != null ? bugLines.size() : 0;
    }
    
    @Override
    public String toString() {
        return "BugInformation{" +
                "hasBugs=" + hasBugs +
                ", bugLines=" + bugLines +
                ", filePath='" + filePath + '\'' +
                '}';
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        
        BugInformation that = (BugInformation) o;
        
        if (hasBugs != that.hasBugs) return false;
        if (bugLines != null ? !bugLines.equals(that.bugLines) : that.bugLines != null) return false;
        return filePath != null ? filePath.equals(that.filePath) : that.filePath == null;
    }
    
    @Override
    public int hashCode() {
        int result = (hasBugs ? 1 : 0);
        result = 31 * result + (bugLines != null ? bugLines.hashCode() : 0);
        result = 31 * result + (filePath != null ? filePath.hashCode() : 0);
        return result;
    }
}