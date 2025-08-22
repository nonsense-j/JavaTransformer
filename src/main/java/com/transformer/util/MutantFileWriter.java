package com.transformer.util;

import com.transformer.api.MutantInfo;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Utility class for writing mutant files with proper naming and header comments.
 */
public class MutantFileWriter {
    
    private static final AtomicInteger globalSequence = new AtomicInteger(1);
    
    /**
     * Generates a mutant header comment in the required format.
     * 
     * @param transformName Name of the transform that was applied
     * @param inputPath Path to the original input file
     * @return Header comment string
     */
    public static String generateMutantHeader(String transformName, String inputPath) {
        // Escape special characters in the input path for the comment
        String escapedInputPath = inputPath != null ? inputPath.replace("\\", "\\\\") : "unknown";
        return String.format("// mutant by transform %s from %s", transformName, escapedInputPath);
    }
    
    /**
     * Generates a filename for a mutant file.
     * 
     * @param inputPath Path to the original input file
     * @param transformName Name of the transform
     * @param sequence Sequence number for this mutant
     * @return Generated filename
     */
    public static String generateMutantFileName(String inputPath, String transformName, int sequence) {
        String inputFileName = getFileNameWithoutExtension(inputPath);
        return String.format("%s_mutant_%s_%d.java", inputFileName, transformName, sequence);
    }
    
    /**
     * Writes a mutant to a file in the specified output directory.
     * 
     * @param mutantCode The mutant code to write
     * @param transformName Name of the transform that was applied
     * @param inputPath Path to the original input file
     * @param outputDir Directory where the mutant file should be saved
     * @return Path to the created mutant file
     * @throws IOException If file writing fails
     */
    public static String writeMutantFile(String mutantCode, String transformName, String inputPath, String outputDir) throws IOException {
        // Create output directory if it doesn't exist
        Path outputDirPath = Paths.get(outputDir);
        if (!Files.exists(outputDirPath)) {
            Files.createDirectories(outputDirPath);
        }
        
        // Generate unique filename
        int sequence = globalSequence.getAndIncrement();
        String fileName = generateMutantFileName(inputPath, transformName, sequence);
        Path outputFilePath = outputDirPath.resolve(fileName);
        
        // Handle naming conflicts by incrementing sequence
        while (Files.exists(outputFilePath)) {
            sequence = globalSequence.getAndIncrement();
            fileName = generateMutantFileName(inputPath, transformName, sequence);
            outputFilePath = outputDirPath.resolve(fileName);
        }
        
        // Generate header comment
        String header = generateMutantHeader(transformName, inputPath);
        
        // Combine header with mutant code
        String finalContent = header + System.lineSeparator() + mutantCode;
        
        // Write to file
        Files.write(outputFilePath, finalContent.getBytes());
        
        return outputFilePath.toString();
    }
    
    /**
     * Creates a MutantInfo with file output.
     * 
     * @param transformName Name of the transform that was applied
     * @param targetNode The AST node that was transformed
     * @param mutantCode The resulting code after transformation
     * @param inputPath Path to the original input file
     * @param outputDir Directory where the mutant file should be saved
     * @return MutantInfo with output file path set
     * @throws IOException If file writing fails
     */
    public static MutantInfo createMutantWithFile(String transformName, org.eclipse.jdt.core.dom.ASTNode targetNode, 
            String mutantCode, String inputPath, String outputDir) throws IOException {
        String outputFilePath = writeMutantFile(mutantCode, transformName, inputPath, outputDir);
        return new MutantInfo(transformName, targetNode, mutantCode, outputFilePath);
    }
    
    /**
     * Extracts the filename without extension from a file path.
     * 
     * @param filePath Full file path
     * @return Filename without extension
     */
    private static String getFileNameWithoutExtension(String filePath) {
        if (filePath == null || filePath.trim().isEmpty()) {
            return "unknown";
        }
        
        Path path = Paths.get(filePath);
        String fileName = path.getFileName().toString();
        
        int lastDotIndex = fileName.lastIndexOf('.');
        if (lastDotIndex > 0) {
            return fileName.substring(0, lastDotIndex);
        }
        return fileName;
    }
    
    /**
     * Resets the global sequence counter (useful for testing).
     */
    public static void resetSequence() {
        globalSequence.set(1);
    }
}