package com.transformer.integration;

import com.transformer.api.TransformerService;
import com.transformer.api.TransformerServiceImpl;
import com.transformer.api.TransformationResult;
import com.transformer.api.BugInformation;
import com.transformer.util.TestUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.After;
import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;

/**
 * Integration tests for TransformerService that validate the complete
 * transformation workflow.
 * Tests end-to-end functionality, error handling, and performance within
 * reasonable limits.
 */
public class TransformerServiceIntegrationTest {

    private TransformerService transformerService;
    private String testOutputDir;
    private String testInputDir;

    @Before
    public void setUp() throws IOException {
        transformerService = new TransformerServiceImpl();
        testOutputDir = "target/test-outputs";
        testInputDir = "src/test/resources/sample-java-files";

        // Create output directory
        Files.createDirectories(Paths.get(testOutputDir));
    }

    @After
    public void tearDown() throws IOException {
        // Clean up test output files
        // TestUtils.cleanupTestFiles(testOutputDir);
    }

    /**
     * Test guided transformation with valid bug information.
     * Validates that guided transformations work correctly with bug data.
     */
    @Test
    public void testGuidedTransformationWithBugInfo() throws IOException {
        // Arrange
        String inputFile = testInputDir + "/AssignExpCross.java";
        String outputDir = testOutputDir;
        BugInformation bugInfo = TestUtils.createTestBugInformation(Arrays.asList(7));

        // Act
        TransformationResult result = transformerService.applyGuidedTransform(
                inputFile, outputDir, bugInfo, "AddBrackets");
        // TransformationResult result = transformerService.applyTargetTransform(
        //         inputFile, outputDir, Arrays.asList(7), "AddBrackets");
        // TransformationResult result = transformerService.applyRandomTransform(inputFile, outputDir, 3, "AddBrackets");
        // Assert
        assertNotNull("Result should not be null", result);

        // Guided transformation might succeed or fail depending on applicable
        // transforms
        assertTrue("Should have generated mutants when transformation succeeds",
                result.hasMutants());
    }
}